package ytex.uima.annotators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.ae.type.NamedEntity;
import edu.mayo.bmi.uima.core.ae.type.OntologyConcept;
import edu.mayo.bmi.uima.core.sentence.type.Sentence;

import ytex.kernel.metric.ConceptSimilarityService.SimilarityMetricEnum;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.wsd.WordSenseDisambiguator;

public class SenseDisambiguatorAnnotator extends JCasAnnotator_ImplBase {
	int windowSize;
	Set<String> conceptIds;
	SimilarityMetricEnum metric;
	WordSenseDisambiguator wsd;
	Properties props;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		props = (Properties) KernelContextHolder.getApplicationContext()
				.getBean("ytexProps");
		props.putAll(System.getProperties());
		windowSize = Integer.parseInt(props.getProperty(
				"ytex.sense.windowSize", "2"));
		metric = SimilarityMetricEnum.valueOf(props.getProperty(
				"ytex.sense.metric", "INTRINSIC_LIN"));
		wsd = KernelContextHolder.getApplicationContext().getBean(
				WordSenseDisambiguator.class);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		// iterate through sentences
		AnnotationIndex sentIdx = jcas.getAnnotationIndex(Sentence.type);
		FSIterator sentIter = sentIdx.iterator();
		AnnotationIndex namedEntityIdx = jcas
				.getAnnotationIndex(NamedEntity.type);
		while (sentIter.hasNext()) {
			Sentence s = (Sentence) sentIter.next();
			// get named entities within each sentence
			FSIterator neIter = namedEntityIdx.subiterator(s);
			// disambiguate the named entities
			disambiguate(neIter);
		}
	}

	/**
	 * 
	 * @param neIter
	 */
	private void disambiguate(FSIterator neIter) {
		// allocate list to hold concepts in each named entity
		List<Set<String>> listConcept = new ArrayList<Set<String>>();
		// allocate corresponding named entity list
		List<NamedEntity> listNE = new ArrayList<NamedEntity>();
		while (neIter.hasNext()) {
			// add the concept senses from each named entity
			Set<String> conceptSenses = new HashSet<String>();
			listConcept.add(conceptSenses);
			NamedEntity ne = (NamedEntity) neIter.next();
			listNE.add(ne);
			FSArray concepts = ne.getOntologyConceptArr();
			for (int i = 0; i < concepts.size(); i++) {
				conceptSenses
						.add(((OntologyConcept) concepts.get(i)).getCode());
			}
		}
		// iterate through named entities and disambiguate
		for (int i = 0; i < listConcept.size(); i++) {
			Set<String> conceptSenses = listConcept.get(i);
			// only bother with wsd if there is more than one sense
			if (conceptSenses.size() > 1) {
				String concept = this.wsd.disambiguate(listConcept, i, null,
						windowSize, metric, null);
				NamedEntity ne = listNE.get(i);
				FSArray concepts = ne.getOntologyConceptArr();
				for (int j = 0; i < concepts.size(); j++) {
					OntologyConcept oc = (OntologyConcept) concepts.get(j);
					if (oc.getCode().equals(concept))
						oc.setOid("1");
				}
			} else {
				// only one concept - set the oid to 1
				NamedEntity ne = listNE.get(i);
				FSArray concepts = ne.getOntologyConceptArr();
				OntologyConcept oc = (OntologyConcept) concepts.get(0);
				oc.setOid("1");
			}
		}
	}
}
