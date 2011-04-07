package ytex.kernel;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * data structure to store instance ids, their classes, folds, runs, and labels.
 * 
 * @author vijay
 * 
 */
public class InstanceData {
	/**
	 * labels - class
	 */
	SortedMap<String, SortedSet<String>> labelToClassMap = new TreeMap<String, SortedSet<String>>();
	/**
	 * map of labels - runs - folds - train/test - instances - class for test
	 * instances
	 */
	SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>>> labelToInstanceMap = new TreeMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>>>();

	public SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>>> getLabelToInstanceMap() {
		return labelToInstanceMap;
	}

	public void setLabelToInstanceMap(
			SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>>> labelToInstanceMap) {
		this.labelToInstanceMap = labelToInstanceMap;
	}

	public SortedMap<String, SortedSet<String>> getLabelToClassMap() {
		return labelToClassMap;
	}

	public void setLabelToClassMap(
			SortedMap<String, SortedSet<String>> labelToClassMap) {
		this.labelToClassMap = labelToClassMap;
	}

}
