package ytex.kernel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

public abstract class BaseSparseDataFormatter implements SparseDataFormatter {

	protected String outdir = null;
	protected Map<String, Integer> numericAttributeMap = new HashMap<String, Integer>();
	protected Map<String, Map<String, Integer>> nominalAttributeMap = new HashMap<String, Map<String, Integer>>();
	protected Map<String, Map<String, Integer>> labelToClassIndexMap = new HashMap<String, Map<String, Integer>>();
	protected int maxAttributeIndex = 0;
	protected Properties exportProperties;

	protected void exportAttributeNames(SparseData sparseData, String label,
			Integer run, Integer fold) throws IOException {
		String filename = FileUtil.getFoldFilePrefix(outdir, label, run, fold);
		if (filename.length() > 0 && !filename.endsWith("/")
				&& !filename.endsWith("\\") && !filename.endsWith("."))
			filename += "_";
		filename += "attributes.txt";
		exportAttributeNames(filename, sparseData);
	}

	/**
	 * assign indices to each attribute.
	 * 
	 * @param outdir
	 *            directory to write file to
	 * @param sparseData
	 * @param numericAttributeMap
	 * @param nominalAttributeMap
	 *            for nominal indices, create an index for each value.
	 * @throws IOException
	 */
	protected int exportAttributeNames(String bFileName, SparseData sparseData)
			throws IOException {
		// libsvm indices 1-based
		int index = 1;
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(bFileName.toString()));
			// add numeric indices
			for (String attributeName : sparseData.getNumericWords()) {
				w.write(attributeName);
				w.newLine();
				numericAttributeMap.put(attributeName, index++);
			}
			// add nominal indices
			for (SortedMap.Entry<String, SortedSet<String>> nominalAttribute : sparseData
					.getNominalWordValueMap().entrySet()) {
				Map<String, Integer> attrValueIndexMap = new HashMap<String, Integer>(
						nominalAttribute.getValue().size());
				for (String attrValue : nominalAttribute.getValue()) {
					w.write(nominalAttribute.getKey());
					if (nominalAttribute.getValue().size() > 1) {
						w.write("\t");
						w.write(attrValue);
					}
					attrValueIndexMap.put(attrValue, index++);
				}
				nominalAttributeMap.put(nominalAttribute.getKey(),
						attrValueIndexMap);
			}
			this.maxAttributeIndex = index;
			return index;
		} finally {
			if (w != null)
				w.close();
		}
	}

	/**
	 * create a map of attribute index - attribute value for the given instance.
	 * 
	 * @param bagOfWordsData
	 * @param numericAttributeMap
	 * @param nominalAttributeMap
	 * @param instanceId
	 * @return
	 */
	protected SortedMap<Integer, Double> getSparseLineValues(
			SparseData bagOfWordsData,
			Map<String, Integer> numericAttributeMap,
			Map<String, Map<String, Integer>> nominalAttributeMap,
			int instanceId) {
		SortedMap<Integer, Double> instanceValues = new TreeMap<Integer, Double>();
		// get numeric values for instance
		if (bagOfWordsData.getInstanceNumericWords().containsKey(instanceId)) {
			for (Map.Entry<String, Double> numericValue : bagOfWordsData
					.getInstanceNumericWords().get(instanceId).entrySet()) {
				// look up index for attribute and put in map
				instanceValues.put(
						numericAttributeMap.get(numericValue.getKey()),
						numericValue.getValue());
			}
		}
		if (bagOfWordsData.getInstanceNominalWords().containsKey(instanceId)) {
			for (Map.Entry<String, String> nominalValue : bagOfWordsData
					.getInstanceNominalWords().get(instanceId).entrySet()) {
				// look up index for attribute and value and put in map
				instanceValues.put(
						nominalAttributeMap.get(nominalValue.getKey()).get(
								nominalValue.getValue()), 1d);
			}
		}
		return instanceValues;
	}

	protected void exportSparseRow(SparseData bagOfWordsData,
			int instanceId, BufferedWriter wData, int row) throws IOException {
		SortedMap<Integer, Double> instanceValues = getSparseLineValues(
				bagOfWordsData, numericAttributeMap, nominalAttributeMap,
				instanceId);
		// write attributes
		// add the attributes
		for (SortedMap.Entry<Integer, Double> instanceValue : instanceValues
				.entrySet()) {
			// row = instance number
			wData.write(Integer.toString(row));
			wData.write("\t");
			// column = attribute index
			wData.write(Integer.toString(instanceValue.getKey()));
			wData.write("\t");
			// value = value
			wData.write(Double.toString(instanceValue.getValue()));
			wData.write("\n");
		}
	}
	protected List<Integer> exportSparseMatrix(
			String filename,
			SparseData sparseData,
			SortedMap<Boolean, SortedMap<Integer, String>> foldInstanceLabelMap)
			throws IOException {
		List<Integer> instanceIds = new ArrayList<Integer>();
		instanceIds.addAll(foldInstanceLabelMap.get(true).keySet());
		Set<Integer> testInstanceIds = foldInstanceLabelMap
				.containsKey(false) ? foldInstanceLabelMap.get(false)
				.keySet() : null;
		if (testInstanceIds != null) {
			testInstanceIds.removeAll(foldInstanceLabelMap.get(true)
					.keySet());
			instanceIds.addAll(testInstanceIds);
		}
		BufferedWriter wData = null;
		try {
			wData = new BufferedWriter(new FileWriter(filename));
			int row = 1;
			for (int instanceId : instanceIds) {
				exportSparseRow(sparseData, instanceId, wData, row);
				row++;
			}
		} finally {
			if (wData != null)
				wData.close();
		}
		return instanceIds;
	}

	/**
	 * get needed properties out of outdir. convert class names into
	 * integers for libsvm. attempt to parse the class name into an integer.
	 * if this fails, use an index that we increment. index corresponds to
	 * class name's alphabetical order.
	 */
	@Override
	public void initializeExport(InstanceData instanceLabel, Properties properties, SparseData sparseData)
			throws IOException {
				this.exportProperties = properties;
				this.outdir = properties.getProperty("outdir");
				FileUtil.createOutdir(outdir);
				for (Map.Entry<String, SortedSet<String>> labelToClass : instanceLabel
						.getLabelToClassMap().entrySet()) {
					Map<String, Integer> classToIndexMap = new HashMap<String, Integer>(
							labelToClass.getValue().size());
					this.labelToClassIndexMap.put(labelToClass.getKey(),
							classToIndexMap);
					int nIndex = 1;
					for (String className : labelToClass.getValue()) {
						Integer classNumber = null;
						try {
							classNumber = Integer.parseInt(className);
						} catch (NumberFormatException fe) {
						}
						if (classNumber == null) {
							classToIndexMap.put(className, nIndex++);
						} else {
							classToIndexMap.put(className, classNumber);
						}
					}
				}
			}
	

}
