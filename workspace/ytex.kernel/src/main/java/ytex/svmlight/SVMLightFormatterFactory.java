package ytex.svmlight;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ytex.kernel.FileUtil;
import ytex.kernel.InstanceData;
import ytex.kernel.SparseData;
import ytex.kernel.SparseDataFormatter;
import ytex.kernel.SparseDataFormatterFactory;
import ytex.libsvm.LibSVMFormatterFactory.LibSVMFormatter;

/**
 * export for svmlight. Same format as for libsvm with following changes:
 * <ul>
 * <li>
 * Only binary classification - classes must be -1 or 1
 * <li>Transductive classification: test instances 'unlabelled' in training set
 * (class 0)
 * <ul/>
 * If a test set is available, will use transductive svm format.
 */
public class SVMLightFormatterFactory implements SparseDataFormatterFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.libsvm.SparseDataFormatterFactory#getFormatter()
	 */
	@Override
	public SparseDataFormatter getFormatter() {
		return new SVMLightFormatter();
	}

	public static class SVMLightFormatter extends LibSVMFormatter {
		protected SortedMap<Boolean, SortedMap<Integer, String>> foldInstanceLabelMap;

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
			if (train && this.foldInstanceLabelMap.size() == 2) {
				// train and test set available - export transductive data
				exportTransductiveData(filename, idFilename, sparseData,
						instanceClassMap, this.foldInstanceLabelMap.get(
								Boolean.FALSE).keySet(),
						this.labelToClassIndexMap.get(label));
			} else {
				// test set, or training set only
				// 'normal' export
				super.exportDataForLabel(filename, idFilename, sparseData,
						instanceClassMap, labelToClassIndexMap.get(label));
			}
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
		 * @return instance ids in order they are in the output file
		 * @throws IOException
		 */
		protected List<Integer> exportTransductiveData(String filename, String idFilename,
				SparseData bagOfWordsData,
				SortedMap<Integer, String> trainClassMap,
				Set<Integer> testInstances, Map<String, Integer> classToIndexMap)
				throws IOException {
			List<Integer> instanceIds = new ArrayList<Integer>();
			BufferedWriter wData = null;
			BufferedWriter wId = null;
			try {
				wData = new BufferedWriter(new FileWriter(filename));
				wId = new BufferedWriter(new FileWriter(idFilename));
				instanceIds.addAll(exportDataForInstances(bagOfWordsData, trainClassMap,
						classToIndexMap, wData, wId));
				SortedMap<Integer, String> testClassMap = new TreeMap<Integer, String>();
				for (Integer instanceId : testInstances) {
					// for sparse datasets may duplicate instances in train/test
					// set. Don't do that for transductive learning
					if (!trainClassMap.containsKey(instanceId))
						testClassMap.put(instanceId, "0");
				}
				instanceIds.addAll(exportDataForInstances(bagOfWordsData, testClassMap,
						classToIndexMap, wData, wId));
				return instanceIds;
			} finally {
				if (wData != null)
					wData.close();
				if (wId != null)
					wId.close();
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
//		protected SortedMap<Integer, Double> getSparseLineValues(
//				SparseData bagOfWordsData,
//				Map<String, Integer> numericAttributeMap,
//				Map<String, Map<String, Integer>> nominalAttributeMap,
//				int instanceId) {
//			SortedMap<Integer, Double> instanceValues = new TreeMap<Integer, Double>();
//			// get numeric values for instance
//			if (bagOfWordsData.getInstanceNumericWords()
//					.containsKey(instanceId)) {
//				for (Map.Entry<String, Double> numericValue : bagOfWordsData
//						.getInstanceNumericWords().get(instanceId).entrySet()) {
//					// look up index for attribute and put in map
//					instanceValues.put(numericAttributeMap.get(numericValue
//							.getKey()), numericValue.getValue());
//				}
//			}
//			if (bagOfWordsData.getInstanceNominalWords()
//					.containsKey(instanceId)) {
//				for (Map.Entry<String, String> nominalValue : bagOfWordsData
//						.getInstanceNominalWords().get(instanceId).entrySet()) {
//					// look up index for attribute and value and put in map
//					instanceValues.put(
//							nominalAttributeMap.get(nominalValue.getKey()).get(
//									nominalValue.getValue()), 1d);
//				}
//			}
//			return instanceValues;
//		}

		/**
		 * add the "0" class for transductive learning
		 */
		@Override
		public void initializeExport(InstanceData instanceLabel,
				Properties properties, SparseData sparseData) throws IOException {
			super.initializeExport(instanceLabel, properties, sparseData);
			for (Map<String, Integer> classIndexMap : labelToClassIndexMap
					.values()) {
				classIndexMap.put("0", 0);
			}
		}

		/**
		 * clean up fold specific state
		 */
		@Override
		public void clearFold() {
			this.numericAttributeMap.clear();
			this.nominalAttributeMap.clear();
			this.foldInstanceLabelMap = null;
		}

		@Override
		public void initializeFold(
				SparseData sparseData,
				String label,
				Integer run,
				Integer fold,
				SortedMap<Boolean, SortedMap<Integer, String>> foldInstanceLabelMap)
				throws IOException {
			super.initializeFold(sparseData, label, run, fold,
					foldInstanceLabelMap);
			this.foldInstanceLabelMap = foldInstanceLabelMap;
		}

	}

}
