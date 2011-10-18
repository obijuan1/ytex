package ytex.kernel;

import java.util.SortedMap;

public interface FoldGenerator {

	/**
	 * Generate cross validation folds, store in database.
	 * 
	 * @param corpusName
	 *            class label
	 * @param query
	 *            query to get instance id - label - class triples
	 * @param nFolds
	 *            number of folds to generate
	 * @param nMinPerClass
	 *            minimum number of instances of each class per fold
	 * @param nSeed
	 *            random number seed; if null will be set currentTime in millis
	 * @param nRuns
	 *            number of runs
	 */
	public abstract void generateRuns(String corpusName, String splitName,
			String query, int nFolds, int nMinPerClass, Integer nSeed, int nRuns);

	/**
	 * Generate cross validation folds, don't store in database.
	 * 
	 * @param labelToInstanceMap
	 *            an instance class map without folds @see
	 *            {@link InstanceData#labelToInstanceMap}
	 * @param nFolds
	 *            number of folds
	 * @param nMinPerClass
	 *            minimum instance per class
	 * @param nSeed
	 *            random seed default to System.currentTimeMillis()
	 * @param nRuns
	 *            number of runs
	 * @param foldMap
	 *            same structure as labelToInstanceMap, but with folds
	 */
	public SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>>> generateRuns(
			SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>>> labelToInstanceMap,
			int nFolds,
			int nMinPerClass,
			Integer nSeed,
			int nRuns);
}