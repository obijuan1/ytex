package ytex.kernel;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import com.google.common.collect.BiMap;

import ytex.kernel.model.KernelEvaluation;

public interface KernelUtil {

	public abstract void loadProperties(String propertyFile, Properties props)
			throws FileNotFoundException, IOException,
			InvalidPropertiesFormatException;

	/**
	 * read query
	 * 
	 * <pre>
	 * [instance id] [class name] [train/test boolean optioanl] [label optional] [fold optional] [run optional]
	 * </pre>
	 * 
	 * return map of
	 * 
	 * <pre>
	 * [label, [run, [fold, [train/test , [instance id, class]]]]]
	 * </pre>
	 * 
	 * <ul>
	 * <li>if label not defined, will be ""
	 * <li>if run not defined, will be 0
	 * <li>if fold not defined, will be 0
	 * <li>if train not defined, will be 1
	 * </ul>
	 * 
	 */
	public abstract InstanceData loadInstances(String strQuery);

	public abstract void fillGramMatrix(
			final KernelEvaluation kernelEvaluation,
			final SortedSet<Long> trainInstanceLabelMap,
			final double[][] trainGramMatrix);

	public abstract double[][] loadGramMatrix(SortedSet<Long> instanceIds,
			String name, String splitName, String experiment, String label,
			int run, int fold, double param1, String param2);

	/**
	 * generate folds from the label to instance map. use properties specified
	 * in props to generate folds.
	 * 
	 * @param instanceLabel
	 * @param props
	 */
	public abstract void generateFolds(InstanceData instanceLabel,
			Properties props);

	public abstract void fillLabelToClassToIndexMap(
			Map<String, SortedSet<String>> labelToClasMap,
			Map<String, BiMap<String, Integer>> labelToClassIndexMap);

	/**
	 * export the class id to class name map.
	 * 
	 * @param classIdMap
	 * @param label
	 * @param run
	 * @param fold
	 * @throws IOException
	 */
	public void exportClassIds(String outdir, Map<String, Integer> classIdMap,
			String label) throws IOException;
}