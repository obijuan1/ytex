package ytex.kernel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import ytex.semil.SemiLFormatterFactory.SemiLDataFormatter;

public class SVMLinFormatterFactory implements SparseDataFormatterFactory {

	@Override
	public SparseDataFormatter getFormatter() {
		return new SVMLinDataFormatter();
	}
	public static class SVMLinDataFormatter extends SemiLDataFormatter {

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
					// write the line
					writeLibsvmLine(wData, instanceValues);
				}
			} finally {
				if(wData != null) {
					wData.close();
				}
			}
		}
		
	}

}
