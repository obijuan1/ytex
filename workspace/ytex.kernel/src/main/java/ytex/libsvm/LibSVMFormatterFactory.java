package ytex.libsvm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import ytex.kernel.FileUtil;
import ytex.kernel.InstanceData;
import ytex.kernel.SparseData;
import ytex.kernel.SparseDataFormatter;
import ytex.kernel.SparseDataFormatterFactory;

public class LibSVMFormatterFactory implements SparseDataFormatterFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.libsvm.SparseDataFormatterFactory#getFormatter()
	 */
	@Override
	public SparseDataFormatter getFormatter() {
		return new LibSVMFormatter();
	}

	public static class LibSVMFormatter implements SparseDataFormatter {
		@Override
		public void initializeLabel(
				String label,
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>> labelInstances,
				Properties properties, SparseData sparseData)  throws IOException {
			// TODO Auto-generated method stub
			
		}

		protected String outdir = null;
		/*
		 * indices to sparse data column for numeric attributes
		 */
		protected Map<String, Integer> numericAttributeMap = new HashMap<String, Integer>();
		/*
		 * indices to sparse data column for nominal attributes
		 */
		protected Map<String, Map<String, Integer>> nominalAttributeMap = new HashMap<String, Map<String, Integer>>();
		/*
		 * class name to index map. classes are sorted by
		 */
		protected Map<String, Map<String, Integer>> labelToClassIndexMap = new HashMap<String, Map<String, Integer>>();
		
		protected int maxAttributeIndex = 0;
		
		protected Properties exportProperties;

		/**
		 * write a file with the attribute names corresponding to the indices in
		 * the libsvm data file
		 */
		@Override
		public void initializeFold(
				SparseData sparseData,
				String label,
				Integer run,
				Integer fold,
				SortedMap<Boolean, SortedMap<Integer, String>> foldInstanceLabelMap)
				throws IOException {
			String filename = FileUtil.getFoldFilePrefix(outdir, label, run,
					fold);
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
		protected int exportAttributeNames(String bFileName,
				SparseData sparseData) throws IOException {
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
		 * export the given train/test set
		 */
		@Override
		public void exportFold(SparseData sparseData,
				SortedMap<Integer, String> instanceClassMap, boolean train,
				String label, Integer run, Integer fold) throws IOException {
			String filename = FileUtil.getDataFilePrefix(outdir, label, run,
					fold, train)
					+ "_data.txt";
			String idFilename = FileUtil.getDataFilePrefix(outdir, label, run,
					fold, train)
					+ "_id.txt";
			exportDataForLabel(filename, idFilename, sparseData,
					instanceClassMap, this.labelToClassIndexMap.get(label));
		}

		/**
		 * Export data file and id file
		 * 
		 * @param filename
		 * @param idFilename
		 * @param bagOfWordsData
		 * @param instanceClassMap
		 * @param numericAttributeMap
		 * @param nominalAttributeMap
		 * @param label
		 * @throws IOException
		 */
		protected void exportDataForLabel(String filename, String idFilename,
				SparseData bagOfWordsData,
				SortedMap<Integer, String> instanceClassMap,
				Map<String, Integer> classToIndexMap) throws IOException {
			BufferedWriter wData = null;
			BufferedWriter wId = null;
			try {
				wData = new BufferedWriter(new FileWriter(filename));
				wId = new BufferedWriter(new FileWriter(idFilename));
				exportDataForInstances(bagOfWordsData, instanceClassMap,
						classToIndexMap, wData, wId);
			} finally {
				if (wData != null)
					wData.close();
				if (wId != null)
					wId.close();
			}
		}

		protected void exportDataForInstances(SparseData bagOfWordsData,
				SortedMap<Integer, String> instanceClassMap,
				Map<String, Integer> classToIndexMap, BufferedWriter wData,
				BufferedWriter wId) throws IOException {
			for (Map.Entry<Integer, String> instanceClass : instanceClassMap
					.entrySet()) {
				int instanceId = instanceClass.getKey();
				// allocate line with sparse attribute indices and values
				SortedMap<Integer, Double> instanceValues = getSparseLineValues(
						bagOfWordsData, numericAttributeMap,
						nominalAttributeMap, instanceId);
				// data file
				// write class id
				int classId = classToIndexMap.get(instanceClass.getValue());
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

		/**
		 * create a map of attribute index - attribute value for the given
		 * instance.
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
			if (bagOfWordsData.getInstanceNumericWords()
					.containsKey(instanceId)) {
				for (Map.Entry<String, Double> numericValue : bagOfWordsData
						.getInstanceNumericWords().get(instanceId).entrySet()) {
					// look up index for attribute and put in map
					instanceValues.put(numericAttributeMap.get(numericValue
							.getKey()), numericValue.getValue());
				}
			}
			if (bagOfWordsData.getInstanceNominalWords()
					.containsKey(instanceId)) {
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
		 * get needed properties out of outdir. convert class names into
		 * integers for libsvm. attempt to parse the class name into an integer.
		 * if this fails, use an index that we increment. index corresponds to
		 * class name's alphabetical order.
		 */
		@Override
		public void initializeExport(InstanceData instanceLabel,
				Properties properties, SparseData sparseData) throws IOException {
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

		/**
		 * clean up fold specific state
		 */
		@Override
		public void clearFold() {
			this.numericAttributeMap.clear();
			this.nominalAttributeMap.clear();
		}

		@Override
		public void clearLabel() {
			// TODO Auto-generated method stub
			
		}
	}

}
