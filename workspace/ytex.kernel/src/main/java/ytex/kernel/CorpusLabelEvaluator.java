package ytex.kernel;

import java.io.IOException;
import java.util.Set;

public interface CorpusLabelEvaluator {

	/*
	 */
	public abstract void evaluateCorpus(String corpusName,
			String conceptGraphName, String conceptSetName, String labelQuery,
			String classFeatureQuery, Double minInfo, Set<String> xVals,
			String xLeftover, String xMerge);

	public abstract boolean evaluateCorpus(String propFile) throws IOException;

}