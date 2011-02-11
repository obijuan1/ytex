package ytex.libsvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculateMetrics {
	// private static final Log log = LogFactory.getLog(CalculateMetrics.class);
	public static Pattern wsPattern = Pattern.compile("\\s");
	public static Pattern wsDotPattern = Pattern.compile("\\s|\\.|\\z");
	public static Pattern totalSVPattern = Pattern.compile("total_sv (\\d+)");
	private static final int IDX_TP = 0;
	private static final int IDX_TN = 1;
	private static final int IDX_FP = 2;
	private static final int IDX_FN = 3;
	private static final int IDX_PREC = 4;
	private static final int IDX_RECALL = 5;
	private static final int IDX_SPEC = 6;
	private static final int IDX_F1 = 7;

	public static void main(String args[]) throws Exception {
		if (args.length < 2) {
			System.out
					.println("compute standard IR metrics (precision, recall, f1-score) from svm output for svms trained on a gram matrix (option -t 4)");
			System.out
					.println("usage: java LibSVMT4Metrics <test gram matrix> <svm_predict output> [model file] [combined output]");
			System.out
					.println("combined output prefixes each line in the svm_predict output with the true class label");
		} else {
			String testGramFile = args[0];
			String libSVMOutput = args[1];
			String modelFile = null;
			String combinedOutput = null;
			if (args.length > 2)
				modelFile = args[2];
			if (args.length > 3)
				combinedOutput = args[3];
			List<String[]> classLabels = new ArrayList<String[]>();
			Set<String> labels = new HashSet<String>();
			processFiles(testGramFile, libSVMOutput, combinedOutput,
					classLabels, labels);
			Map<String, double[]> metrics = computeMetrics(classLabels, labels);
			outputMetrics(metrics, modelFile);
		}
	}

	private static void outputMetrics(Map<String, double[]> metrics,
			String modelFile) throws IOException {
		for (Map.Entry<String, double[]> entry : metrics.entrySet()) {
			MessageFormat doubleFmt = new MessageFormat("{0,number,#.###}");
			System.out.print(entry.getKey());
			double classMetric[] = entry.getValue();
			for (int i = 0; i < IDX_FN; i++) {
				System.out.print("\t");
				System.out.print((int) classMetric[i]);
			}
			for (int i = IDX_FN; i < classMetric.length; i++) {
				System.out.print("\t");
				System.out.print(doubleFmt.format(new Object[] { new Double(
						classMetric[i]) }));
			}
			if (modelFile != null) {
				String totalSV = getTotalSV(modelFile);
				System.out.print("\t");
				if (totalSV != null) {
					System.out.print(totalSV);
				}
			}
			System.out.println();
		}
	}

	/**
	 * 
	 * @param classLabels
	 *            list of instances' true and predicted class labels
	 * @param labels
	 *            list of unique class labels
	 * @return map of label-array of metrics
	 */
	private static Map<String, double[]> computeMetrics(
			List<String[]> classLabels, Set<String> labels) {
		Map<String, double[]> metrics = new TreeMap<String, double[]>();
		for (String metricLabel : labels) {
			/*
			 * calculate tp,fp,tn,fn relative to label iterate through each
			 * class label to compute these
			 */
			double labelMetric[] = new double[IDX_F1 + 1];
			metrics.put(metricLabel, labelMetric);
			for (String[] instanceLabel : classLabels) {
				String trueLabel = instanceLabel[0];
				String predLabel = instanceLabel[1];
				if (trueLabel.equals(metricLabel)) {
					if (predLabel.equals(trueLabel)) {
						labelMetric[IDX_TP]++;
					} else {
						labelMetric[IDX_FN]++;
					}
				} else {
					if (predLabel.equals(metricLabel)) {
						labelMetric[IDX_FP]++;
					} else {
						labelMetric[IDX_TN]++;
					}
				}
			}
			computeLabelMetrics(labelMetric);
		}
		return metrics;
	}

	/**
	 * calculate precision, recall, sensitivity, f1-score
	 * 
	 * @param labelMetric
	 */
	private static void computeLabelMetrics(double[] labelMetric) {
		labelMetric[IDX_PREC] = (labelMetric[IDX_TP] + labelMetric[IDX_FP]) > 0 ? labelMetric[IDX_TP]
				/ (labelMetric[IDX_TP] + labelMetric[IDX_FP])
				: 0;
		labelMetric[IDX_RECALL] = (labelMetric[IDX_TP] + labelMetric[IDX_FN]) > 0 ? labelMetric[IDX_TP]
				/ (labelMetric[IDX_TP] + labelMetric[IDX_FN])
				: 0;
		labelMetric[IDX_SPEC] = (labelMetric[IDX_TN] + labelMetric[IDX_FP]) > 0 ? labelMetric[IDX_TN]
				/ (labelMetric[IDX_TN] + labelMetric[IDX_FP])
				: 0;
		labelMetric[IDX_F1] = (labelMetric[IDX_RECALL] + labelMetric[IDX_PREC]) > 0 ? 2
				* labelMetric[IDX_PREC]
				* labelMetric[IDX_RECALL]
				/ (labelMetric[IDX_RECALL] + labelMetric[IDX_PREC])
				: 0;
	}

	private static void processFiles(String testGramFile, String libSVMOutput,
			String combinedOutput, List<String[]> classLabels,
			Set<String> labels) throws FileNotFoundException, IOException {
		BufferedReader testGramReader = null;
		BufferedReader libSVMOutputReader = null;
		BufferedWriter combinedOutputWriter = null;
		try {
			testGramReader = new BufferedReader(new FileReader(testGramFile));
			libSVMOutputReader = new BufferedReader(
					new FileReader(libSVMOutput));
			if (combinedOutput != null)
				combinedOutputWriter = new BufferedWriter(new FileWriter(
						combinedOutput));
			String gramLine = null;
			String svmOutLine = null;
			int nLine = 0;
			// 1st line in libSVMOutputReader lists labels
			libSVMOutputReader.readLine();
			while (((gramLine = testGramReader.readLine()) != null)
					&& ((svmOutLine = libSVMOutputReader.readLine()) != null)) {
				nLine++;
				String predictTokens[] = wsPattern.split(svmOutLine);
				String labelPredict = predictTokens[0];
				String labelTruth = extractFirstToken(gramLine, wsPattern);
				if (labelTruth != null && labelPredict != null) {
					classLabels.add(new String[] { labelTruth, labelPredict });
					if (!labels.contains(labelPredict))
						labels.add(labelPredict);
					if (!labels.contains(labelTruth))
						labels.add(labelTruth);
				}
				if (combinedOutputWriter != null) {
					combinedOutputWriter.write(labelTruth);
					combinedOutputWriter.write("\t");
					combinedOutputWriter.write(svmOutLine);
					combinedOutputWriter.newLine();
				}
			}
		} finally {
			if (testGramReader != null) {
				try {
					testGramReader.close();
				} catch (Exception e) {
					System.err.println("testGramReader");
					e.printStackTrace(System.err);
				}
			}
			if (libSVMOutputReader != null) {
				try {
					libSVMOutputReader.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
			if (combinedOutputWriter != null) {
				try {
					combinedOutputWriter.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	public static String extractFirstToken(String line, Pattern tokDelimPattern) {
		Matcher wsMatcher = tokDelimPattern.matcher(line);
		String token = null;
		if (wsMatcher.find() && wsMatcher.start() > 0) {
			token = line.substring(0, wsMatcher.start());
		}
		return token;
	}

	private static String getTotalSV(String modelFile) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(modelFile));
			String line = null;
			while ((line = r.readLine()) != null) {
				Matcher m = totalSVPattern.matcher(line);
				if (m.find()) {
					return m.group(1);
				}
			}
		} finally {
			try {
				if (r != null)
					r.close();
			} catch (Exception e) {
				System.err.println("reading model file");
				e.printStackTrace(System.err);
			}
		}
		return null;
	}
}
