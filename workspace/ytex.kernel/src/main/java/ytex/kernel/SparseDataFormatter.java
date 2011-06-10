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
	 * scope property key
	 */
	public static final String SCOPE = "scope";
	/**
	 * fold value for scope
	 */
	public static final String SCOPE_FOLD = "fold";
	/**
	 * label value for scope
	 */
	public static final String SCOPE_LABEL = "label";
	/**
	 * value <tt>instance_id</tt>. SparseMatrix adds the instance_id attribute
	 * to the matrix. This is a reserved attribute name.
	 */
	public static final String ATTR_INSTANCE_ID = "instance_id";

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
			Integer fold,
			SortedMap<Boolean, SortedMap<Integer, String>> foldInstanceLabelMap)
			throws IOException;

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
	void exportFold(SparseData sparseData,
			SortedMap<Integer, String> sortedMap, boolean train, String label,
			Integer run, Integer fold) throws IOException;

	/**
	 * initialize export - called once
	 * 
	 * @param instanceLabel
	 * @param properties
	 * @throws IOException
	 */
	void initializeExport(InstanceData instanceLabel, Properties properties,
			SparseData sparseData) throws IOException;

	/**
	 * clear all data structures set up during initializeFold
	 */
	void clearFold();

	void initializeLabel(
			String label,
			SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>> labelInstances,
			Properties properties, SparseData sparseData) throws IOException;

	void clearLabel();

}
