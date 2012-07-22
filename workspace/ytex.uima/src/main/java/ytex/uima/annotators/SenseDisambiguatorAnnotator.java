package ytex.uima.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Strings;

import ytex.kernel.metric.ConceptSimilarityService;
import ytex.kernel.metric.ConceptSimilarityService.SimilarityMetricEnum;
import ytex.kernel.wsd.WordSenseDisambiguator;
import ytex.uima.ApplicationContextHolder;
import ytex.uima.types.OntologyConcept;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * Disambiguate named entities via adapated Lesk algorithm with semantic
 * similarity. Configuration parameters set in ytex.properties / via -D option:
 * <ul>
 * <li>ytex.sense.windowSize - context window size. concepts from named entities
 * +- windowSize around the target named entity are used for disambiguation.
 * defaults to 10
 * <li>ytex.sense.metric - measure to use. defaults to INTRINSIC_PATH
 * <li>ytex.conceptGraph - concept graph to use.
 * </ul>
 * 
 * If you are using ytex.uima.types.OntologyConcept, this annotator will set the
 * predictedConcept and score attributes. If you are using cTAKES
 * OntologyConcept, this annotator will throw out concepts from the named entity
 * that didn't score highest.
 * 
 * @author vijay
 * 
 */
public class SenseDisambiguatorAnnotator extends JCasAnnotator_ImplBase {
	int windowSize;
	Set<String> conceptIds;
	SimilarityMetricEnum metric;
	WordSenseDisambiguator wsd;
	Properties props;
	boolean disabled = false;
	private static final Log log = LogFactory
			.getLog(SenseDisambiguatorAnnotator.class);

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		props = ApplicationContextHolder.getYtexProperties();
		Integer nWindowSize = (Integer) aContext
				.getConfigParameterValue("windowSize");
		if (nWindowSize != null && nWindowSize.intValue() > 0)
			windowSize = nWindowSize.intValue();
		else
			windowSize = Integer.parseInt(props.getProperty(
					"ytex.sense.windowSize", "50"));
		String uMetric = (String)aContext.getConfigParameterValue("metric");
		if (!Strings.isNullOrEmpty(uMetric))
			metric = SimilarityMetricEnum.valueOf(uMetric);
		else
			metric = SimilarityMetricEnum.valueOf(props.getProperty(
					"ytex.sense.metric", "INTRINSIC_PATH"));
		wsd = ApplicationContextHolder.getSimApplicationContext().getBean(
				WordSenseDisambiguator.class);
		ConceptSimilarityService simSvc = ApplicationContextHolder
				.getSimApplicationContext().getBean(
						ConceptSimilarityService.class);
		if (simSvc.getConceptGraph() == null) {
			log.warn("Concept Graph was not loaded - word sense disambiguation disabled");
			disabled = true;
		}
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		if (disabled)
			return;
		// iterate through sentences
		FSIterator<Annotation> neIter = jcas.getAnnotationIndex(
				IdentifiedAnnotation.type).iterator();
		List<IdentifiedAnnotation> listNE = new ArrayList<IdentifiedAnnotation>();
		while (neIter.hasNext()) {
			listNE.add((IdentifiedAnnotation) neIter.next());
		}
		// disambiguate the named entities
		disambiguate(jcas, listNE);
	}

	/**
	 * 
	 * @param jcas
	 * @param listNE
	 *            list of named entities to disambiguate
	 */
	protected void disambiguate(JCas jcas, List<IdentifiedAnnotation> listNE) {
		// allocate list to hold IdentifiedAnnotations with concepts
		List<IdentifiedAnnotation> listNonTrivialNE = new ArrayList<IdentifiedAnnotation>();
		// allocate list to hold concepts in each named entity
		List<Set<String>> listConcept = new ArrayList<Set<String>>();
		for (IdentifiedAnnotation ne : listNE) {
			FSArray concepts = ne.getOntologyConceptArr();
			// add the concept senses from each named entity
			if (concepts != null && concepts.size() > 0) {
				listNonTrivialNE.add(ne);
				Set<String> conceptSenses = new HashSet<String>();
				listConcept.add(conceptSenses);
				for (int i = 0; i < concepts.size(); i++) {
					if (concepts.get(i) != null
							&& ((OntologyConcept) concepts.get(i)).getCode() != null)
						conceptSenses.add(((OntologyConcept) concepts.get(i))
								.getCode());
				}
			}
		}
		// iterate through named entities and disambiguate
		for (int i = 0; i < listConcept.size(); i++) {
			Set<String> conceptSenses = listConcept.get(i);
			// only bother with wsd if there is more than one sense
			if (conceptSenses.size() > 1) {
				Map<String, Double> scores = new HashMap<String, Double>();
				String concept = this.wsd.disambiguate(listConcept, i, null,
						windowSize, metric, scores, true);
				IdentifiedAnnotation ne = listNonTrivialNE.get(i);
				FSArray concepts = ne.getOntologyConceptArr();
				for (int j = 0; j < concepts.size(); j++) {
					OntologyConcept oc = (OntologyConcept) concepts.get(j);
					if (oc instanceof ytex.uima.types.OntologyConcept) {
						// for ytex ontology concepts update the score and set
						// the
						// predicted concept field
						ytex.uima.types.OntologyConcept yoc = (ytex.uima.types.OntologyConcept) oc;
						if (concept == null || concept.equals(oc.getCode()))
							yoc.setDisambiguated(true);
						if (scores.containsKey(oc.getCode()))
							yoc.setScore(scores.get(oc.getCode()));
					} else {
						// for other ontology concepts, throw out the concepts
						// that didn't get the top score
						if (concept != null && concept.equals(oc.getCode())) {
							FSArray ocArr = new FSArray(jcas, 1);
							ocArr.set(0, oc);
							ne.setOntologyConceptArr(ocArr);
						}

					}
				}
			} else if (conceptSenses.size() == 1) {
				// only one concept - for ytex concept set the predicted concept
				IdentifiedAnnotation ne = listNonTrivialNE.get(i);
				FSArray concepts = ne.getOntologyConceptArr();
				OntologyConcept oc = (OntologyConcept) concepts.get(0);
				if (oc instanceof ytex.uima.types.OntologyConcept) {
					((ytex.uima.types.OntologyConcept) oc)
							.setDisambiguated(true);
				}
			}
		}
	}
}
