package ytex.libsvm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

import ytex.kernel.BaseSparseDataFormatter;
import ytex.kernel.FileUtil;
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

	public static class LibSVMFormatter extends BaseSparseDataFormatter {
		@Override
		public void initializeLabel(
				String label,
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>> labelInstances,
				Properties properties, SparseData sparseData)
				throws IOException {
			// TODO Auto-generated method stub

		}

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
			exportAttributeNames(sparseData, label, run, fold);
		}

		/**
		 * export the given train/test set
		 */
		@Override
		public void exportFold(SparseData sparseData,
				SortedMap<Integer, String> instanceClassMap, boolean train,
				String label, Integer run, Integer fold) throws IOException {
			String filename = FileUtil.getDataFilePrefix(outdir, label, run,
					fold, train) + "_data.txt";
			String idFilename = FileUtil.getDataFilePrefix(outdir, label, run,
					fold, train) + "_id.txt";
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

		/**
		 * 
		 * @param bagOfWordsData
		 *            data to be exported
		 * @param instanceClassMap
		 *            instance ids - class name map
		 * @param classToIndexMap
		 *            class name - class id map
		 * @param wData
		 *            file to write data to
		 * @param wId
		 *            file to write ids to
		 * @return list of instance ids corresponding to order with which they
		 *         were exported
		 * @throws IOException
		 */
		protected List<Integer> exportDataForInstances(
				SparseData bagOfWordsData,
				SortedMap<Integer, String> instanceClassMap,
				Map<String, Integer> classToIndexMap, BufferedWriter wData,
				BufferedWriter wId) throws IOException {
			List<Integer> instanceIds = new ArrayList<Integer>();
			for (Map.Entry<Integer, String> instanceClass : instanceClassMap
					.entrySet()) {
				int instanceId = instanceClass.getKey();
				instanceIds.add(instanceId);
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
			return instanceIds;
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
		}
	}

}
