package ytex.kernel.wsd;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ytex.kernel.metric.ConceptSimilarityService.SimilarityMetricEnum;

public interface WordSenseDisambiguator {

	public abstract String disambiguate(List<Set<String>> sentenceConcepts,
			int index, Set<String> contextConcepts, int windowSize,
			SimilarityMetricEnum metric, Map<String, Double> scoreMap);

}