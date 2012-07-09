package ytex.kernel.wsd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ytex.kernel.metric.ConceptPairSimilarity;
import ytex.kernel.metric.ConceptSimilarityService;
import ytex.kernel.metric.ConceptSimilarityService.SimilarityMetricEnum;

public class WordSenseDisambiguatorImpl implements WordSenseDisambiguator {
	ConceptSimilarityService conceptSimilarityService;

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	@Override
	public String disambiguate(List<Set<String>> sentenceConcepts, int index,
			Set<String> contextConcepts, int windowSize,
			SimilarityMetricEnum metric, Map<String, Double> scoreMap) {
		return disambiguate(sentenceConcepts, index, contextConcepts,
				windowSize, metric, scoreMap, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.wsd.WordSenseDisambiguator#disambiguate(java.util.List,
	 * int, java.util.Set, int,
	 * ytex.kernel.ConceptSimilarityService.SimilarityMetricEnum, java.util.Map)
	 */
	@Override
	public String disambiguate(List<Set<String>> sentenceConcepts, int index,
			Set<String> contextConcepts, int windowSize,
			SimilarityMetricEnum metric, Map<String, Double> scoreMap,
			boolean weighted) {
		// get the candidate concepts that we want to disambiguate
		Set<String> candidateConcepts = sentenceConcepts.get(index);
		if (candidateConcepts.size() == 1)
			return candidateConcepts.iterator().next();
		// allocate set to hold all the concepts to compare to
		Map<String, Integer> windowContextConcepts = new HashMap<String, Integer>();
		// add context concepts (e.g. title concepts)
		if (contextConcepts != null) {
			addConcepts(windowContextConcepts, contextConcepts);
		}
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
				addConcepts(windowContextConcepts, cs);
			}
		}
		if (indexRightStart > index + 1) {
			for (Set<String> cs : sentenceConcepts.subList(index + 1,
					indexRightStart)) {
				addConcepts(windowContextConcepts, cs);
			}
		}
		// allocate map to hold scores
		SortedMap<Double, String> scoreConceptMap = new TreeMap<Double, String>();
		for (String c : candidateConcepts) {
			scoreConceptMap
					.put(scoreConcept(c, windowContextConcepts, metric,
							weighted), c);
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

	private void addConcepts(Map<String, Integer> windowContextConcepts,
			Set<String> contextConcepts) {
		for (String c : contextConcepts) {
			Integer cn = windowContextConcepts.get(c);
			if (cn != null) {
				windowContextConcepts.put(c, cn + 1);
			} else {
				windowContextConcepts.put(c, 1);
			}
		}
	}

	private double scoreConcept(String concept,
			Map<String, Integer> windowContextConcepts,
			SimilarityMetricEnum metric, boolean weighted) {
		List<SimilarityMetricEnum> metrics = Arrays.asList(metric);
		double score = 0d;
		for (Map.Entry<String, Integer> windowConcept : windowContextConcepts
				.entrySet()) {
			ConceptPairSimilarity csim = conceptSimilarityService.similarity(
					metrics, concept, windowConcept.getKey(), null, false);
			if (weighted)
				score += csim.getSimilarities().get(0)
						* windowConcept.getValue().doubleValue();
			else
				score += csim.getSimilarities().get(0);
		}
		return score;
	}

}
