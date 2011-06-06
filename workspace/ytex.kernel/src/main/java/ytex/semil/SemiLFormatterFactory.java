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
import java.util.TreeSet;

import ytex.kernel.FileUtil;
import ytex.kernel.InstanceData;
import ytex.kernel.SparseData;
import ytex.kernel.SparseDataFormatter;
import ytex.kernel.SparseDataFormatterFactory;
import ytex.svmlight.SVMLightFormatterFactory.SVMLightFormatter;
import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.doublealgo.Statistic.VectorVectorFunction;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * for each fold produce 3 files:
 * <ul>
 * <li>_id.txt - contains instance ids and class labels. 3 columns: instance id,
 * train/test, class
 * <li>_data.txt - distance matrix. 3 columns: row index - column index -
 * distance
 * <li>_data.txt.lbl - label file. class labels corresponding to rows. test data
 * automatically unlabeled
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
		SortedSet<Integer> instanceIds;
		/**
		 * cosine distance: <tt>1-aa'/sqrt(aa' * bb')</tt>
		 */
		public static Statistic.VectorVectorFunction COSINE = new VectorVectorFunction() {
			DoubleDoubleFunction fun = new DoubleDoubleFunction() {
				public final double apply(double a, double b) {
					return Math.abs(a - b) / Math.abs(a + b);
				}
			};

			public final double apply(DoubleMatrix1D a, DoubleMatrix1D b) {
				double ab = a.zDotProduct(b);
				double sqrt_ab = Math.sqrt(a.zDotProduct(a) * b.zDotProduct(b));
				return 1 - ab / sqrt_ab;
			}
		};

		@Override
		public void initializeExport(InstanceData instanceLabel,
				Properties properties, SparseData sparseData)
				throws IOException {
			super.initializeExport(instanceLabel, properties, sparseData);
			this.instanceLabel = instanceLabel;
			if (properties.getProperty("scope") == null
					|| properties.getProperty("scope").length() == 0) {
				exportAttributeNames(
						FileUtil.getFoldFilePrefix(outdir, null, null, null),
						sparseData);
				// all instance ids assumed to be identical across all folds
				SortedMap<Boolean, SortedMap<Integer, String>> fold = instanceLabel
						.getLabelToInstanceMap().values().iterator().next()/* runs */
						.values().iterator().next() /* folds */
						.values().iterator().next();
				// get all instance Ids
				SortedSet<Integer> instanceIds = new TreeSet<Integer>();
				initializeInstanceIds(fold, instanceIds);
				exportDistance(sparseData, null, null, null);
			}
		}

		private void initializeInstanceIds(
				SortedMap<Boolean, SortedMap<Integer, String>> fold,
				SortedSet<Integer> instanceIds) {
			instanceIds.addAll(fold.get(true).keySet());
			instanceIds.addAll(fold.get(true).keySet());
			this.instanceIds = instanceIds;
		}

		@Override
		public void initializeLabel(
				String label,
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>> labelInstances,
				Properties properties, SparseData sparseData)
				throws IOException {
			super.initializeLabel(label, labelInstances, properties, sparseData);
			if ("label".equals(this.exportProperties.getProperty("scope"))) {
				exportAttributeNames(
						FileUtil.getFoldFilePrefix(outdir, label, null, null),
						sparseData);
				SortedMap<Boolean, SortedMap<Integer, String>> fold = labelInstances
						.values().iterator().next().values().iterator().next();
				initializeInstanceIds(fold, instanceIds);
				exportDistance(sparseData, label, null, null);
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
				exportAttributeNames(
						FileUtil.getFoldFilePrefix(outdir, label, run, fold),
						sparseData);
				initializeInstanceIds(foldInstanceLabelMap, instanceIds);
				exportDistance(sparseData, label, run, fold);
			}
			String idFilename = FileUtil.getFoldFilePrefix(outdir, label, run,
					fold) + "_id.txt";
			String lblFilename = FileUtil.getFoldFilePrefix(outdir, label, run,
					fold) + "_data.txt.lbl";
			// train and test set available - export transductive data
			export(idFilename, lblFilename, sparseData,
					foldInstanceLabelMap.get(true), foldInstanceLabelMap.get(false),
					this.labelToClassIndexMap.get(label));
		}

		private void exportDistance(SparseData sparseData, String label,
				Integer run, Integer fold) throws IOException {
			SparseDoubleMatrix2D data = new SparseDoubleMatrix2D(
					this.instanceIds.size(), maxAttributeIndex);
			int row = 0;
			for (Integer instanceId : this.instanceIds) {
				// write row to sparse data matrix
				// get 'vector'
				SortedMap<Integer, Double> instanceValues = getSparseLineValues(
						sparseData, numericAttributeMap, nominalAttributeMap,
						instanceId);
				// write it to the matrix
				for (SortedMap.Entry<Integer, Double> instanceValue : instanceValues
						.entrySet()) {
					// row = instance number
					// column = attribute index
					// value = value
					data.set(row, instanceValue.getKey() - 1,
							instanceValue.getValue());
				}
				// increment row index
				row++;
			}
			String filename = FileUtil.getFoldFilePrefix(outdir, label, run,
					fold) + "dist.txt";
			this.writeDistanceMatrix(data, filename);
		}

		@Override
		public void exportFold(SparseData sparseData,
				SortedMap<Integer, String> instanceClassMap, boolean train,
				String label, Integer run, Integer fold) throws IOException {
			//do nothing
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
		private void export(String idFilename,
				String lblFilename, SparseData bagOfWordsData,
				SortedMap<Integer, String> trainInstanceClassMap,
				SortedMap<Integer, String> testInstanceClassMap,
				Map<String, Integer> classToIndexMap) throws IOException {
			BufferedWriter wId = null;
			BufferedWriter wLabel = null;
			try {
				wId = new BufferedWriter(new FileWriter(idFilename));
				wLabel = new BufferedWriter(new FileWriter(lblFilename));
				for(Integer instanceId : this.instanceIds) {
					// for training default to unlabeled
					int classIdTrain = 0;
					if(trainInstanceClassMap.containsKey(instanceId)) {
						// if the instance is in the training set, then use that label
						classIdTrain = classToIndexMap.get(trainInstanceClassMap.get(instanceId));
					}
					// check test set for gold class
					int classIdGold = 0;
					if(testInstanceClassMap.containsKey(instanceId))
						classIdGold = classToIndexMap.get(testInstanceClassMap.get(instanceId));
					else
						classIdGold = classIdTrain;
					// write instance id, if this is in the train set, and it's class
					wId.write(Integer.toString(instanceId));
					wId.write("\t");
					wId.write(trainInstanceClassMap.containsKey(instanceId) ? "1" : "0");
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
		private void writeDistanceMatrix(SparseDoubleMatrix2D data,
				String filename) throws IOException {
//			String distanceFuncName = this.exportProperties.getProperty(
//					"distance", "EUCLID");
//			Statistic.VectorVectorFunction func = Statistic.EUCLID;
//			if ("COSINE".equalsIgnoreCase(distanceFuncName)) {
//				func = COSINE;
//			}
//			DoubleMatrix2D dist = Statistic.distance(data, func);
//			BufferedWriter wData = null;
//			try {
//				wData = new BufferedWriter(new FileWriter(filename));
//				for (int row = 1; row < dist.rows(); row++) {
//					for (int col = row + 1; col < dist.columns(); col++) {
//						double d = dist.get(row, col);
//						if (d < 0.999) {
//							wData.write(Integer.toString(row + 1));
//							wData.write("    ");
//							wData.write(Integer.toString(col + 1));
//							wData.write("    ");
//							wData.write(semilNumberFormat.format(round(d, 6)));
//							wData.write("\n");
//						}
//					}
//				}
//			} finally {
//				if (wData != null)
//					try {
//						wData.close();
//					} catch (Exception ignore) {
//					}
//			}
		}

		/**
		 * round double to specified precision
		 * 
		 * @param Rval
		 * @param Rpl
		 * @return
		 */
		private double round(double Rval, int Rpl) {
			double p = (double) Math.pow(10, Rpl);
			Rval = Rval * p;
			double tmp = Math.round(Rval);
			return (double) tmp / p;
		}

	}

}
