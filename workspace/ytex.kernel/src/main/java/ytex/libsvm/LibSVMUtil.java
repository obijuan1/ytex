package ytex.libsvm;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public interface LibSVMUtil {

	/**
	 * @param strQuery
	 *            query to get instance id - class label
	 * @param labels
	 *            fill with distinct labels
	 * @return Map[Instance ID, Map[Class Label, Class Id]]
	 */
	public abstract SortedMap<Integer, Map<String, Integer>> loadClassLabels(
			String strQuery, final Set<String> labels);
	
	public void outputInstanceIds(String outdir,
			SortedMap<Integer, Map<String, Integer>> trainInstanceLabelMap,
			String string) throws IOException;

}