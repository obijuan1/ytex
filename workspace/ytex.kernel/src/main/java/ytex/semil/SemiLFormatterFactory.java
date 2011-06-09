package ytex.semil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import ytex.kernel.FileUtil;
import ytex.kernel.InstanceData;
import ytex.kernel.SparseData;
import ytex.kernel.SparseDataExporter.ScopeEnum;
import ytex.kernel.SparseDataFormatter;
import ytex.kernel.SparseDataFormatterFactory;
import ytex.svmlight.SVMLightFormatterFactory.SVMLightFormatter;

/**
 * Export data for use with SemiL. I would have liked to have computed the
 * distance using the COLT library; however this was far too slow.
 * 
 * for each fold produce 3 files:
 * <ul>
 * <li>_id.txt - contains instance ids and class labels. 3 columns: instance id,
 * train/test, class
 * <li>_data.txt - sparse data file with class labels of first fold. This must
 * be converted into a distance matrix using semiL. By default in libsvm format
 * (compatible with SemiL). Can be exported in sparseMatrix format for so that
 * you can compute the distance yourself in e.g. R or Matlab.
 * <li>_data.txt.lbl - label file, on for each fold. class labels corresponding
 * to rows. test data automatically unlabeled. The same distance matrix can be
 * used with different label files - the rows across folds refer to the same
 * instance ids. What differs is the labels for the test instances (0 for test
 * instance's fold).
 * </ul>
 * 
 * @author vhacongarlav
 * 
 */
public class SemiLFormatterFactory implements SparseDataFormatterFactory {

	@Override
	public SparseDataFormatter getFormatter() {
		return new SemiLDataFormatter();
	}

	public static class SemiLDataFormatter extends SVMLightFormatter {
		NumberFormat semilNumberFormat = new DecimalFormat("#.######");
		InstanceData instanceLabel = null;
		/**
		 * instance ids of the current data set we are exporting. This is set in
		 * exportSemiL - a single file is exported for all folds. This is read
		 * in exportFold - for each fold, we generate a different label file.
		 */
		List<Integer> instanceIds;

		// /**
		// * cosine distance: <tt>1-aa'/sqrt(aa' * bb')</tt>
		// */
		// public static Statistic.VectorVectorFunction COSINE = new
		// VectorVectorFunction() {
		// DoubleDoubleFunction fun = new DoubleDoubleFunction() {
		// public final double apply(double a, double b) {
		// return Math.abs(a - b) / Math.abs(a + b);
		// }
		// };
		//
		// public final double apply(DoubleMatrix1D a, DoubleMatrix1D b) {
		// double ab = a.zDotProduct(b);
		// double sqrt_ab = Math.sqrt(a.zDotProduct(a) * b.zDotProduct(b));
		// return 1 - ab / sqrt_ab;
		// }
		// };

		@Override
		public void initializeExport(InstanceData instanceLabel,
				Properties properties, SparseData sparseData)
				throws IOException {
			super.initializeExport(instanceLabel, properties, sparseData);
			this.instanceLabel = instanceLabel;
			if (properties.getProperty("scope") == null
					|| properties.getProperty("scope").length() == 0) {
				// all instance ids assumed to be identical across all folds
				SortedMap<Boolean, SortedMap<Integer, String>> fold = instanceLabel
						.getLabelToInstanceMap().values().iterator().next()/* runs */
						.values().iterator().next() /* folds */
						.values().iterator().next();
				exportSemiL(fold, sparseData, null, null, null);
			}
		}

		@Override
		public void initializeLabel(
				String label,
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>> labelInstances,
				Properties properties, SparseData sparseData)
				throws IOException {
			super.initializeLabel(label, labelInstances, properties, sparseData);
			if ("label".equals(this.exportProperties.getProperty("scope"))) {
				SortedMap<Boolean, SortedMap<Integer, String>> fold = labelInstances
						.values().iterator().next().values().iterator().next();
				exportSemiL(fold, sparseData, label, null, null);
			}
		}

		@Override
		public void initializeFold(
				SparseData sparseData,
				String label,
				Integer run,
				Integer fold,
				SortedMap<Boolean, SortedMap<Integer, String>> foldInstanceLabelMap)
				throws IOException {
			if ("fold".equals(this.exportProperties.getProperty("scope"))) {
				exportSemiL(foldInstanceLabelMap, sparseData, label, run, fold);
			}
			String labelFileName = FileUtil.getFoldFilePrefix(outdir, label,
					run, fold) + "_label.txt";
			String idFileName = FileUtil.getFoldFilePrefix(outdir, label, run,
					fold) + "_class.txt";
			exportLabel(idFileName, labelFileName,
					foldInstanceLabelMap.get(true),
					foldInstanceLabelMap.get(false),
					this.labelToClassIndexMap.get(label));
		}

		/**
		 * 
		 * @param foldInstanceLabelMap
		 * @param sparseData
		 * @param label
		 * @param run
		 * @param fold
		 * @throws IOException
		 */
		private void exportSemiL(
				SortedMap<Boolean, SortedMap<Integer, String>> foldInstanceLabelMap,
				SparseData sparseData, String label, Integer run, Integer fold)
				throws IOException {
			exportAttributeNames(sparseData, label, run, fold);
			String filename = FileUtil.getFoldFilePrefix(outdir, label, run,
					fold) + "_data.txt";
			String idFilename = FileUtil.getFoldFilePrefix(outdir, label, run,
					fold) + "_id.txt";
			String format = this.exportProperties.getProperty("semil.format",
					"libsvm");
			if ("sparseMatrix".equals(format)) {
				this.instanceIds = exportSparseMatrix(filename, sparseData,
						foldInstanceLabelMap);
			} else {
				this.instanceIds = exportTransductiveData(filename, idFilename,
						sparseData, foldInstanceLabelMap.get(Boolean.TRUE),
						foldInstanceLabelMap.get(Boolean.FALSE).keySet(),
						this.labelToClassIndexMap.get(label));
			}
		}

		// private void exportDistance(SparseData sparseData, String label,
		// Integer run, Integer fold) throws IOException {
		// SparseDoubleMatrix2D data = new SparseDoubleMatrix2D(
		// this.instanceIds.size(), maxAttributeIndex);
		// int row = 0;
		// for (Integer instanceId : this.instanceIds) {
		// // write row to sparse data matrix
		// // get 'vector'
		// SortedMap<Integer, Double> instanceValues = getSparseLineValues(
		// sparseData, numericAttributeMap, nominalAttributeMap,
		// instanceId);
		// // write it to the matrix
		// for (SortedMap.Entry<Integer, Double> instanceValue : instanceValues
		// .entrySet()) {
		// // row = instance number
		// // column = attribute index
		// // value = value
		// data.set(row, instanceValue.getKey() - 1,
		// instanceValue.getValue());
		// }
		// // increment row index
		// row++;
		// }
		// String filename = FileUtil.getFoldFilePrefix(outdir, label, run,
		// fold) + "dist.txt";
		// this.writeDistanceMatrix(data, filename);
		// }


		@Override
		public void exportFold(SparseData sparseData,
				SortedMap<Integer, String> instanceClassMap, boolean train,
				String label, Integer run, Integer fold) throws IOException {
			// do nothing
		}

		/**
		 * export the data
		 * 
		 * @param filename
		 * @param idFilename
		 * @param lblFilename
		 * @param bagOfWordsData
		 * @param trainInstanceClassMap
		 * @param testInstanceClassMap
		 * @param classToIndexMap
		 * @throws IOException
		 */
		private void exportLabel(String idFilename, String lblFilename,
				SortedMap<Integer, String> trainInstanceClassMap,
				SortedMap<Integer, String> testInstanceClassMap,
				Map<String, Integer> classToIndexMap) throws IOException {
			BufferedWriter wId = null;
			BufferedWriter wLabel = null;
			try {
				wId = new BufferedWriter(new FileWriter(idFilename));
				wLabel = new BufferedWriter(new FileWriter(lblFilename));
				for (Integer instanceId : this.instanceIds) {
					// for training default to unlabeled
					int classIdTrain = 0;
					if (trainInstanceClassMap.containsKey(instanceId)) {
						// if the instance is in the training set, then use that
						// label
						classIdTrain = classToIndexMap
								.get(trainInstanceClassMap.get(instanceId));
					}
					// check test set for gold class
					int classIdGold = 0;
					if (testInstanceClassMap.containsKey(instanceId))
						classIdGold = classToIndexMap.get(testInstanceClassMap
								.get(instanceId));
					else
						classIdGold = classIdTrain;
					// write instance id, if this is in the train set, and it's
					// class
					wId.write(Integer.toString(instanceId));
					wId.write("\t");
					wId.write(trainInstanceClassMap.containsKey(instanceId) ? "1"
							: "0");
					wId.write("\t");
					wId.write(Integer.toString(classIdGold));
					wId.write("\n");
					// write label file for semiL
					wLabel.write(Integer.toString(classIdTrain));
					wLabel.write("\n");
				}
			} finally {
				if (wId != null)
					try {
						wId.close();
					} catch (Exception ignore) {
					}
				if (wLabel != null)
					try {
						wLabel.close();
					} catch (Exception ignore) {
					}
			}
		}

		/**
		 * write distance up to 6 digit precision. only write distance if &lt;
		 * 0.999. format: <tt>
		 * row column dist
		 * </tt> 1-based indices.
		 * 
		 * @todo - 0.999 also for euclidean distance??
		 * 
		 * @param data
		 * @param wData
		 * @throws IOException
		 */
		// private void writeDistanceMatrix(SparseDoubleMatrix2D data,
		// String filename) throws IOException {
		// String distanceFuncName = this.exportProperties.getProperty(
		// "distance", "EUCLID");
		// Statistic.VectorVectorFunction func = Statistic.EUCLID;
		// if ("COSINE".equalsIgnoreCase(distanceFuncName)) {
		// func = COSINE;
		// }
		// DoubleMatrix2D dist = Statistic.distance(data, func);
		// BufferedWriter wData = null;
		// try {
		// wData = new BufferedWriter(new FileWriter(filename));
		// for (int row = 1; row < dist.rows(); row++) {
		// for (int col = row + 1; col < dist.columns(); col++) {
		// double d = dist.get(row, col);
		// if (d < 0.999) {
		// wData.write(Integer.toString(row + 1));
		// wData.write("    ");
		// wData.write(Integer.toString(col + 1));
		// wData.write("    ");
		// wData.write(semilNumberFormat.format(round(d, 6)));
		// wData.write("\n");
		// }
		// }
		// }
		// } finally {
		// if (wData != null)
		// try {
		// wData.close();
		// } catch (Exception ignore) {
		// }
		// }
		// }

		// /**
		// * round double to specified precision
		// *
		// * @param Rval
		// * @param Rpl
		// * @return
		// */
		// private double round(double Rval, int Rpl) {
		// double p = (double) Math.pow(10, Rpl);
		// Rval = Rval * p;
		// double tmp = Math.round(Rval);
		// return (double) tmp / p;
		// }

	}

}
