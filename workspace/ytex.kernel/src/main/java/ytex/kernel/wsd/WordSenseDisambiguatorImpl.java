package ytex.kernel.wsd;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ytex.kernel.ConceptSimilarityService;
import ytex.kernel.ConceptSimilarityService.SimilarityMetricEnum;

public class WordSenseDisambiguatorImpl implements WordSenseDisambiguator {
	ConceptSimilarityService conceptSimilarityService;

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	/* (non-Javadoc)
	 * @see ytex.kernel.wsd.WordSenseDisambiguator#disambiguate(java.util.List, int, java.util.Set, int, ytex.kernel.ConceptSimilarityService.SimilarityMetricEnum, java.util.Map)
	 */
	@Override
	public String disambiguate(List<Set<String>> sentenceConcepts, int index,
			Set<String> contextConcepts, int windowSize,
			SimilarityMetricEnum metric, Map<String, Double> scoreMap) {
		// get the candidate concepts that we want to disambiguate
		Set<String> candidateConcepts = sentenceConcepts.get(index);
		if (candidateConcepts.size() == 1)
			return candidateConcepts.iterator().next();
		// allocate set to hold all the concepts to compare to
		Set<String> windowContextConcepts = new HashSet<String>();
		// add context concepts (e.g. title concepts)
		if (contextConcepts != null)
			windowContextConcepts.addAll(contextConcepts);
		// add windowSize concepts from the sentence
		// get left, then right concepts
		// case 1 - enough tokens on both sides
		int indexLeftStart = index - windowSize - 1;
		int indexRightStart = index + windowSize + 1;
		if (indexLeftStart < 0) {
			// case 2 - not enough tokens on left
			indexRightStart += (-1 * indexLeftStart);
			indexLeftStart = 0;
		} else if (indexRightStart >= sentenceConcepts.size()) {
			// case 3 - not enough tokens on right
			indexLeftStart -= indexRightStart - sentenceConcepts.size() - 1;
			indexRightStart = sentenceConcepts.size() - 1;
		}
		// make sure the range is in bounds
		if (indexLeftStart < 0)
			indexLeftStart = 0;
		if (indexRightStart >= sentenceConcepts.size())
			indexRightStart = sentenceConcepts.size() - 1;
		// add the concepts in the ranges
		if (indexLeftStart < index - 1) {
			for (Set<String> cs : sentenceConcepts.subList(indexLeftStart,
					index - 1)) {
				windowContextConcepts.addAll(cs);
			}
		}
		if (indexRightStart > index + 1) {
			for (Set<String> cs : sentenceConcepts.subList(index + 1,
					indexRightStart)) {
				windowContextConcepts.addAll(cs);
			}
		}
		// allocate map to hold scores
		SortedMap<Double, String> scoreConceptMap = new TreeMap<Double, String>();
		for (String c : candidateConcepts) {
			scoreConceptMap.put(scoreConcept(c, windowContextConcepts, metric),
					c);
		}
		// if scoreMap is not null, fill it in with the concept scores - invert
		// scoreConceptMap
		if (scoreMap != null) {
			for (Map.Entry<Double, String> scoreConcept : scoreConceptMap
					.entrySet()) {
				scoreMap.put(scoreConcept.getValue(), scoreConcept.getKey());
			}
		}
		// get the best scoring concept
		return scoreConceptMap.get(scoreConceptMap.lastKey());
	}

	private double scoreConcept(String concept,
			Set<String> windowContextConcepts, SimilarityMetricEnum metric) {
		Set<SimilarityMetricEnum> metrics = new HashSet<SimilarityMetricEnum>();
		metrics.add(metric);
		double score = 0d;
		for (String windowConcept : windowContextConcepts) {
			Map<SimilarityMetricEnum, Double> sim = conceptSimilarityService
					.similarity(metrics, concept, windowConcept, null, null);
			if (sim.containsKey(metric))
				score += sim.get(metric);
		}
		return score;
	}

}
