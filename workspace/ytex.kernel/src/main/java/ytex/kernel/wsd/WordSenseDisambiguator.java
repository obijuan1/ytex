package ytex.kernel.wsd;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ytex.kernel.metric.ConceptSimilarityService.SimilarityMetricEnum;

public interface WordSenseDisambiguator {

	public abstract String disambiguate(List<Set<String>> sentenceConcepts,
			int index, Set<String> contextConcepts, int windowSize,
			SimilarityMetricEnum metric, Map<String, Double> scoreMap);

	/**
	 * Disambiguate a named entity.
	 * 
	 * @param sentenceConcepts
	 *            named entities from the document, represented as list of
	 *            sets of concept ids
	 * @param index
	 *            index of target named entity to disambiguate
	 * @param contextConcepts
	 *            context concepts, e.g. from title
	 * @param windowSize
	 *            number of named entities on either side of target to use for
	 *            disambiguation
	 * @param metric
	 *            metric to use
	 * @param scoreMap
	 *            optional to get the scores assigned to each concept
	 * @param weighted
	 *            to weight context concepts by frequency
	 * @return highest scoring concept, or null if none of the target concepts
	 *         are in the concept graph, or if all the target concepts have the
	 *         same score
	 */
	String disambiguate(List<Set<String>> sentenceConcepts, int index,
			Set<String> contextConcepts, int windowSize,
			SimilarityMetricEnum metric, Map<String, Double> scoreMap,
			boolean weighted);

}