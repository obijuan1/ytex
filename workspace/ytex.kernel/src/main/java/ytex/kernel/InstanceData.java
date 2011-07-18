package ytex.kernel;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
	SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>>> labelToInstanceMap = new TreeMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>>>();

	public SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>>> getLabelToInstanceMap() {
		return labelToInstanceMap;
	}

	public void setLabelToInstanceMap(
			SortedMap<String, SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>>> labelToInstanceMap) {
		this.labelToInstanceMap = labelToInstanceMap;
	}

	public SortedMap<String, SortedSet<String>> getLabelToClassMap() {
		return labelToClassMap;
	}

	public void setLabelToClassMap(
			SortedMap<String, SortedSet<String>> labelToClassMap) {
		this.labelToClassMap = labelToClassMap;
	}

	/**
	 * get all the instance ids for the specified scope
	 * 
	 * @param label
	 *            if null, then all instance ids, else if run & fold = 0, then
	 *            all instance ids for this label.
	 * @param run
	 * @param fold
	 *            if run & fold != 0, then all instance ids for the specified
	 *            fold
	 * @return
	 */
	public SortedSet<Long> getAllInstanceIds(String label, int run, int fold) {
		SortedSet<Long> instanceIds = new TreeSet<Long>();
		if (label == null) {
			for (String labelKey : this.getLabelToInstanceMap().keySet()) {
				instanceIds.addAll(getAllInstanceIds(labelKey, 0, 0));
			}
		} else if (label != null && fold == 0 && run == 0) {
			for (int runKey : this.getLabelToInstanceMap().get(label).keySet()) {
				for (int foldKey : this.getLabelToInstanceMap().get(label)
						.get(runKey).keySet()) {
					instanceIds
							.addAll(getAllInstanceIds(label, runKey, foldKey));
				}
			}
		}
		if (fold != 0 && run != 0) {
			for (SortedMap<Long, String> foldInst : this
					.getLabelToInstanceMap().get(label).get(run).get(fold)
					.values()) {
				instanceIds.addAll(foldInst.keySet());
			}
		}
		return instanceIds;
	}
}
