package ytex.kernel;

import java.io.IOException;
import java.util.Properties;
import java.util.SortedMap;

/**
 * stateful class called by the sparseDataExporter to export sparse data in a
 * specific format. This is created, called for each fold, training, and test
 * set, then thrown away.
 * 
 * @author vijay
 * 
 */
public interface SparseDataFormatter {

	/**
	 * initialize data structures for the fold that will be exported. called
	 * before export.
	 * 
	 * @param sparseData
	 * @param label
	 * @param run
	 * @param fold
	 * @param foldInstanceLabelMap 
	 * @throws IOException
	 */
	void initializeFold(SparseData sparseData, String label, Integer run,
			Integer fold, SortedMap<Boolean,SortedMap<Integer,String>> foldInstanceLabelMap) throws IOException;

	/**
	 * export the fold train/test set. called once per train/test set, 2x per
	 * fold.
	 * 
	 * @param sparseData
	 * @param sortedMap
	 * @param train
	 * @param label
	 * @param run
	 * @param fold
	 * @throws IOException
	 */
	void export(SparseData sparseData, SortedMap<Integer, String> sortedMap,
			boolean train, String label, Integer run, Integer fold)
			throws IOException;

	/**
	 * initialize export - called once
	 * 
	 * @param instanceLabel
	 * @param properties
	 * @throws IOException
	 */
	void initializeInstances(InstanceData instanceLabel, Properties properties)
			throws IOException;

	/**
	 * clear all data structures set up during initializeFold
	 */
	void clearFold();

}
