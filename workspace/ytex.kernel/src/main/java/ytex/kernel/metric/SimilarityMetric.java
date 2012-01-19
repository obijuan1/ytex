package ytex.kernel.metric;

import java.util.Map;

public interface SimilarityMetric {
	/**
	 * 
	 * @param concept1
	 *            required - concept id
	 * @param concept2
	 *            required - concept id
	 * @param conceptFilter
	 *            optional. map of concept id to relevance (infogain) for all
	 *            concepts. Only lcses from this map will be considered.
	 * @param simInfo
	 *            optional. if provided, we will fill in the path information
	 *            and lcs of simInfo
	 * @return similarity
	 */
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo);


}
