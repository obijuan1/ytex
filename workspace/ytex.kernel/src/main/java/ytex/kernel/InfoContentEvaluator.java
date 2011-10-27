package ytex.kernel;

import java.util.Map;

public interface InfoContentEvaluator {

	public static final String INFOCONTENT = "infocontent";

	/**
	 * calculate information content for all concepts
	 */
	public abstract void evaluateCorpusInfoContent(String freqQuery,
			String corpusName, String conceptGraphName, String conceptSetName);

	public abstract Map<String, Double> getFrequencies(String freqQuery);

}