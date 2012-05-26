package ytex.uima.annotators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import ytex.uima.types.OntologyConcept;

import edu.mayo.bmi.uima.core.type.NamedEntity;

/**
 * Disambiguate MetaMap Concepts. Create NamedEntity annotations for each set of
 * CandidateConcept annotations that span the same text. Pass these NamedEntity
 * annotations to the disambiguator. Save these annotations.
 * 
 * @author vijay
 * 
 */
public class MetaMapSenseDisambiguatorAnnotator extends
		SenseDisambiguatorAnnotator {
	Log log = LogFactory.getLog(MetaMapSenseDisambiguatorAnnotator.class);

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		if (disabled)
			return;
		// get the Metamap type
		Type candidateType = jcas.getTypeSystem().getType(
				"org.metamap.uima.ts.Candidate");
		// abort if the type is not found
		if (candidateType == null) {
			log.debug("no candidate concepts");
			return;
		}
		// get the cui feature
		Feature cuiFeature = candidateType.getFeatureByBaseName("cui");
		if (cuiFeature == null) {
			log.warn("no cui feature!");
			return;
		}
		// iterate through candidates
		FSIterator<Annotation> candidateIter = jcas.getAnnotationIndex(
				candidateType).iterator();
		List<NamedEntity> listNE = new ArrayList<NamedEntity>();
		//
		NamedEntity neLast = null;
		Set<String> concepts = new HashSet<String>();
		while (candidateIter.hasNext()) {
			Annotation annoCandidate = candidateIter.next();
			if (neLast != null && neLast.getBegin() == annoCandidate.getBegin()
					&& neLast.getEnd() == annoCandidate.getEnd()) {
				// this candidate spans the same text as the last named entity
				// add it as one of the concepts
				concepts.add(annoCandidate.getStringValue(cuiFeature));
			} else {
				// moving on to a new named entity, finalize the old one
				addConcepts(jcas, candidateIter, listNE, neLast, concepts);
				// allocate a new named entity
				neLast = new NamedEntity(jcas);
				neLast.setBegin(annoCandidate.getBegin());
				neLast.setEnd(annoCandidate.getEnd());
				concepts.add(annoCandidate.getStringValue(cuiFeature));
			}
		}
		addConcepts(jcas, candidateIter, listNE, neLast, concepts);
		// disambiguate the named entities
		disambiguate(jcas, listNE);
		// save the named entities
		for (NamedEntity ne : listNE) {
			ne.addToIndexes();
		}
	}

	/**
	 * create the ontology concept array, add it to the named entity
	 * 
	 * @param jcas
	 * @param candidateIter
	 * @param listNE
	 * @param neLast
	 * @param concepts
	 */
	private void addConcepts(JCas jcas, FSIterator<Annotation> candidateIter,
			List<NamedEntity> listNE, NamedEntity neLast,
			Set<String> concepts) {
		if (neLast != null) {
			// finalize the NamedEntity
			FSArray ocArr = new FSArray(jcas, concepts.size());
			int ocArrIdx = 0;
			for (String c : concepts) {
				// set the cui field if this is in fact a cui
				OntologyConcept oc = new OntologyConcept(jcas);
				oc.setCode(c);
				ocArr.set(ocArrIdx, oc);
				ocArrIdx++;
			}
			neLast.setOntologyConceptArr(ocArr);
			concepts.clear();
			listNE.add(neLast);
		}
	}

}
