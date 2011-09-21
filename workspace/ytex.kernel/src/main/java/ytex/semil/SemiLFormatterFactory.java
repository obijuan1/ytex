package ytex.semil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;

import ytex.kernel.FileUtil;
import ytex.kernel.InstanceData;
import ytex.kernel.SparseData;
import ytex.kernel.SparseDataFormatter;
import ytex.kernel.SparseDataFormatterFactory;
import ytex.svmlight.SVMLightFormatterFactory.SVMLightFormatter;

/**
 * Export data for use with SemiL. I would have liked to have computed the
 * distance using the COLT library; however this was far too slow.
 * 
 * Produce following files:
 * <ul>
 * <li>[scope]_data.txt - sparse data file. Can be converted into distance
 * matrix for SemiL using R or Matlab. R script provided. If you want to use
 * semiL to generate a distance matrix, use the libsvm formatter.
 * <li>[fold]_label.txt - semiL label file, one for each fold. class labels
 * corresponding to rows. test data automatically unlabeled. The same
 * data/distance matrix can be used with different label files - the rows across
 * folds refer to the same instance ids. What differs is the labels for the test
 * instances (0 for test instance's fold).
 * <li>[fold]_class.txt - contains instance ids and target class ids for each
 * fold. matrix with 3 columns: instance id, train/test, target class id. Used
 * by SemiLEvaluationParser to evaluate SemiL predictions.
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
			if (properties.getProperty(SCOPE) == null
					|| properties.getProperty(SCOPE).length() == 0) {
				exportSemiL(sparseData, null, null, null);
			}
		}

		@Override
		public void initializeLabel(
				String label,
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>> labelInstances,
				Properties properties, SparseData sparseData)
				throws IOException {
			super.initializeLabel(label, labelInstances, properties, sparseData);
			if (SCOPE_LABEL.equals(this.exportProperties.getProperty(SCOPE))) {
				exportSemiL(sparseData, label, null, null);
			}
		}

		@Override
		public void initializeFold(
				SparseData sparseData,
				String label,
				Integer run,
				Integer fold,
				SortedMap<Boolean, SortedMap<Long, String>> foldInstanceLabelMap)
				throws IOException {
			if (SCOPE_FOLD.equals(this.exportProperties.getProperty(SCOPE))) {
				exportSemiL(sparseData, label, run, fold);
			}
			String labelFileName = FileUtil.getScopedFileName(outdir, label,
					run, fold, "label.txt");
			String idFileName = FileUtil.getScopedFileName(outdir, label, run,
					fold, "class.txt");
			exportLabel(idFileName, labelFileName,
					foldInstanceLabelMap.get(true),
					foldInstanceLabelMap.get(false),
					this.labelToClassIndexMap.get(label),
					sparseData.getInstanceIds());
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
		private void exportSemiL(SparseData sparseData, String label,
				Integer run, Integer fold) throws IOException {
			exportAttributeNames(sparseData, label, run, fold);
			String filename = FileUtil.getScopedFileName(outdir, label, run,
					fold, "data.txt");
			exportSparseMatrix(filename, sparseData);

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
				SortedMap<Long, String> instanceClassMap, boolean train,
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
				SortedMap<Long, String> trainInstanceClassMap,
				SortedMap<Long, String> testInstanceClassMap,
				Map<String, Integer> classToIndexMap,
				SortedSet<Long> instanceIds) throws IOException {
			BufferedWriter wId = null;
			BufferedWriter wLabel = null;
			try {
				wId = new BufferedWriter(new FileWriter(idFilename));
				wLabel = new BufferedWriter(new FileWriter(lblFilename));
				for (Long instanceId : instanceIds) {
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
					if (testInstanceClassMap != null && testInstanceClassMap.containsKey(instanceId))
						classIdGold = classToIndexMap.get(testInstanceClassMap
								.get(instanceId));
					else
						classIdGold = classIdTrain;
					// write instance id, if this is in the train set, and it's
					// class
					wId.write(Long.toString(instanceId));
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
