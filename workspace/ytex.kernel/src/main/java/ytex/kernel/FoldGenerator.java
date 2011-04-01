package ytex.kernel;

public interface FoldGenerator {

	/**
	 * Generate cross validation folds, store in database
	 * @param name class label
	 * @param query query to get instance id - label - class triples
	 * @param nFolds number of folds to generate
	 * @param nMinPerClass minimum number of instances of each class per fold
	 * @param nSeed random number seed; if null will be set currentTime in millis
	 * @param nRuns number of runs
	 */
	public abstract void generateRuns(String name, String query, int nFolds,
			int nMinPerClass, Integer nSeed, int nRuns);

}