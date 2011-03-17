package ytex.libsvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import ytex.kernel.AbstractBagOfWordsExporter;
import ytex.kernel.BagOfWordsData;
import ytex.kernel.BagOfWordsExporter;

/**
 * property file entries: <li>filePrefix - directory and file base name for
 * exported files <li>instanceClassQuery - query to retrieve class labels <li>
 * numericWordQuery - query to retrieve numeric attributes <li>nominalWordQuery
 * - query to retrieve nominal attributes <li>tfidf - true/false
 * 
 * @author vijay
 * 
 */
public class LibSVMBagOfWordsExporterImpl extends AbstractBagOfWordsExporter
		implements BagOfWordsExporter {
	LibSVMUtil libsvmUtil;

	public LibSVMUtil getLibsvmUtil() {
		return libsvmUtil;
	}

	public void setLibsvmUtil(LibSVMUtil libsvmUtil) {
		this.libsvmUtil = libsvmUtil;
	}

	public void exportBagOfWords(String propertyFile) throws IOException {
		Properties props = new Properties();
		loadProperties(propertyFile, props);
		String outdir = props.getProperty("outdir");
		if (outdir == null || outdir.length() == 0)
			outdir = ".";
		exportBagOfWords(outdir, props.getProperty("train.instance.query"),
				props.getProperty("test.instance.query"),
				props.getProperty("numericWordQuery", ""),
				props.getProperty("nominalWordQuery", ""),
				"true".equals(props.getProperty("tfidf", "false")));
	}

	public void exportBagOfWords(String outdir, String trainInstanceQuery,
			String testInstanceQuery, String numericWordQuery,
			String nominalWordQuery, boolean tfIdf) throws IOException {
		BagOfWordsData bagOfWordsData = new BagOfWordsData();
		loadData(bagOfWordsData, numericWordQuery, nominalWordQuery, null,
				tfIdf);
		Set<String> labels = new HashSet<String>();
		SortedMap<Integer, Map<String, Integer>> trainInstanceLabelMap = libsvmUtil
				.loadClassLabels(trainInstanceQuery, labels);
		SortedMap<Integer, Map<String, Integer>> testInstanceLabelMap = null;
		if (testInstanceQuery != null) {
			testInstanceLabelMap = libsvmUtil.loadClassLabels(
					testInstanceQuery, labels);
		}
		Map<String, Integer> numericAttributeMap = new HashMap<String, Integer>();
		Map<String, Map<String, Integer>> nominalAttributeMap = new HashMap<String, Map<String, Integer>>();
		exportAttributeNames(outdir, bagOfWordsData, numericAttributeMap,
				nominalAttributeMap);
		exportData(outdir, "training", bagOfWordsData, labels,
				trainInstanceLabelMap, numericAttributeMap, nominalAttributeMap);
		if (testInstanceLabelMap != null) {
			exportData(outdir, "test", bagOfWordsData, labels,
					testInstanceLabelMap, numericAttributeMap,
					nominalAttributeMap);
		}
	}

	private void exportData(String outdir, String type,
			BagOfWordsData bagOfWordsData, Set<String> labels,
			SortedMap<Integer, Map<String, Integer>> instanceLabelMap,
			Map<String, Integer> numericAttributeMap,
			Map<String, Map<String, Integer>> nominalAttributeMap)
			throws IOException {
		for (String label : labels) {
			StringBuilder bFileName = new StringBuilder(outdir)
					.append(File.separator).append(type).append("_data_")
					.append(label).append(".txt");
			StringBuilder bInstanceIdFileName = new StringBuilder(outdir)
			.append(File.separator).append(type).append("_id_")
			.append(label).append(".txt");
			exportDataForLabel(bFileName.toString(), bInstanceIdFileName.toString(), bagOfWordsData,
					instanceLabelMap, numericAttributeMap, nominalAttributeMap,
					label);
		}
	}

	/**
	 * Export data file and id file
	 * @param filename
	 * @param idFilename
	 * @param bagOfWordsData
	 * @param instanceLabelMap
	 * @param numericAttributeMap
	 * @param nominalAttributeMap
	 * @param label
	 * @throws IOException
	 */
	private void exportDataForLabel(String filename, String idFilename,
			BagOfWordsData bagOfWordsData,
			SortedMap<Integer, Map<String, Integer>> instanceLabelMap,
			Map<String, Integer> numericAttributeMap,
			Map<String, Map<String, Integer>> nominalAttributeMap, String label)
			throws IOException {
		BufferedWriter wData = null;
		BufferedWriter wId = null;
		try {
			wData = new BufferedWriter(new FileWriter(filename));
			wId = new BufferedWriter(new FileWriter(idFilename));
			for (Map.Entry<Integer, Map<String, Integer>> instanceClass : instanceLabelMap
					.entrySet()) {
				int instanceId = instanceClass.getKey();
				// allocate line with sparse attribute indices and values
				SortedMap<Integer, Double> instanceValues = getSparseLineValues(
						bagOfWordsData, numericAttributeMap,
						nominalAttributeMap, instanceId);
				// data file
				// write class id
				if (instanceClass.getValue() != null
						&& instanceClass.getValue().containsKey(label)) {
					int classId = instanceClass.getValue().get(label);
					// write id to id file
					wId.write(Integer.toString(instanceId));
					wId.newLine();
					wData.write(Integer.toString(classId));
					// write attributes
					// add the attributes
					for (SortedMap.Entry<Integer, Double> instanceValue : instanceValues
							.entrySet()) {
						wData.write("\t");
						wData.write(Integer.toString(instanceValue.getKey()));
						wData.write(":");
						wData.write(Double.toString(instanceValue.getValue()));
					}
					wData.newLine();
				}
			}
		} finally {
			if (wData != null)
				wData.close();
			if (wId != null)
				wId.close();
		}
	}

	private SortedMap<Integer, Double> getSparseLineValues(
			BagOfWordsData bagOfWordsData,
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

	/**
	 * assign indices to each attribute.
	 * 
	 * @param filePrefix
	 * @param bagOfWordsData
	 * @param numericAttributeMap
	 * @param nominalAttributeMap
	 *            for nominal indices, create an index for each value.
	 * @throws IOException
	 */
	private void exportAttributeNames(String outdir,
			BagOfWordsData bagOfWordsData,
			Map<String, Integer> numericAttributeMap,
			Map<String, Map<String, Integer>> nominalAttributeMap)
			throws IOException {
		// libsvm indices 1-based
		int index = 1;
		BufferedWriter w = null;
		try {
			StringBuilder bFileName = new StringBuilder(outdir).append(
					File.separator).append("attributes.txt");
			w = new BufferedWriter(new FileWriter(bFileName.toString()));
			// add numeric indices
			for (String attributeName : bagOfWordsData.getNumericWords()) {
				w.write(attributeName);
				w.newLine();
				numericAttributeMap.put(attributeName, index++);
			}
			// add nominal indices
			for (SortedMap.Entry<String, SortedSet<String>> nominalAttribute : bagOfWordsData
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
		} finally {
			if (w != null)
				w.close();
		}
	}
}
