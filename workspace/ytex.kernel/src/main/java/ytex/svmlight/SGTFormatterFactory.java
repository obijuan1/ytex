package ytex.svmlight;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;

import ytex.kernel.FileUtil;
import ytex.kernel.SparseData;
import ytex.kernel.SparseDataFormatter;
import ytex.kernel.SparseDataFormatterFactory;
import ytex.semil.SemiLFormatterFactory.SemiLDataFormatter;

/**
 * For each scope, create a data.txt file. The SGT tools will be used to convert
 * this into an adjacency graph. For each train/test fold, create a label and
 * class file. The label file will be used by sgt for prediction, the class file
 * will be used to parse the results.
 * 
 * @author vijay
 * 
 */
public class SGTFormatterFactory implements SparseDataFormatterFactory {

	@Override
	public SparseDataFormatter getFormatter() {
		return new SGTFormatter();
	}

	public static class SGTFormatter extends SemiLDataFormatter {
		@Override
		protected void exportData(SparseData sparseData, String label,
				Integer run, Integer fold) throws IOException {
			exportAttributeNames(sparseData, label, run, fold);
			String filename = FileUtil.getScopedFileName(outdir, label, run,
					fold, "data.txt");
			BufferedWriter wData = null;
			try {
				wData = new BufferedWriter(new FileWriter(filename));
				for (long instanceId : sparseData.getInstanceIds()) {
					// get line with sparse attribute indices and values
					SortedMap<Integer, Double> instanceValues = getSparseLineValues(
							sparseData, numericAttributeMap,
							nominalAttributeMap, instanceId);
					// the class is irrelevant - we create label files used by
					// sgt
					wData.write(Integer.toString(0));
					// write the line
					writeLibsvmLine(wData, instanceValues);
				}
			} finally {
				if (wData != null) {
					wData.close();
				}
			}
		}
	}

}
