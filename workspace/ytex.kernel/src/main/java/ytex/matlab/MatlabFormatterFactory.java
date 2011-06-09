package ytex.matlab;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;

import ytex.kernel.SparseData;
import ytex.kernel.SparseDataFormatter;
import ytex.kernel.SparseDataFormatterFactory;
import ytex.libsvm.LibSVMFormatterFactory.LibSVMFormatter;

public class MatlabFormatterFactory implements SparseDataFormatterFactory {

	@Override
	public SparseDataFormatter getFormatter() {
		return new MatlabDataFormatter();
	}
	
	public static class MatlabDataFormatter extends LibSVMFormatter {

		/**
		 * id - export 2-column matrix
		 * column 1 - instance id
		 * column 2 - class id
		 * 
		 * data - export 3-column matrix
		 * column 1 - row index
		 * column 2 - column index
		 * column 3 - value
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
				// iterate over rows
				int row = 1;
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
					wId.write(Integer.toString(instanceId));
					wId.write("\t");
					wId.write(Integer.toString(classId));
					wId.write("\n");
					// write attributes
					exportSparseRow(bagOfWordsData, instanceId, wData, row);
					// increment row index
					row++;
				}
			} finally {
				if (wData != null)
					wData.close();
				if (wId != null)
					wId.close();
			}
		}		
	}

}
