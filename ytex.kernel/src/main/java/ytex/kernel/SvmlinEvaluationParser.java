package ytex.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.model.SVMClassifierEvaluation;

public class SvmlinEvaluationParser extends BaseClassifierEvaluationParser {
	private static final Log log = LogFactory
			.getLog(SvmlinEvaluationParser.class);
	public static Pattern pAlgo = Pattern.compile("-A\\s+(\\d)");
	public static Pattern pLambdaW = Pattern.compile("-W\\s+([\\d\\.eE-]+)");
	public static Pattern pLambaU = Pattern.compile("-U\\s+([\\d\\.eE-]+)");

	/**
	 * parse directory. Expect following files:
	 * <ul>
	 * <li>model.txt - libsvm model file
	 * <li>options.properties - properties file with needed parameter settings
	 * (see ParseOption)
	 * <li>predict.txt - predictions on test set
	 * </ul>
	 */
	@Override
	public void parseDirectory(File dataDir, File outputDir) throws IOException {
		String optionsFile = outputDir.getPath() + File.separator
				+ "options.properties";
		if (checkFileRead(optionsFile)) {
			// read options.properties
			Properties props = this.loadProps(outputDir);
			SVMClassifierEvaluation eval = new SVMClassifierEvaluation();
			// set algorithm
			eval.setAlgorithm("svmlin");
			// parse results
			parseResults(dataDir, outputDir, eval, props);
			// save the classifier evaluation
			storeSemiSupervised(props, eval);
		}
	}

	private void parseResults(File dataDir, File outputDir,
			SVMClassifierEvaluation eval, Properties props) throws IOException {
		// parse fold, run, label from file base name
		String fileBaseName = this.getFileBaseName(props);
		initClassifierEvaluation(fileBaseName, eval);
		// initialize common properties
		initClassifierEvaluationFromProperties(props, eval);
		// parse options from command line
		String options = props
				.getProperty(ParseOption.EVAL_LINE.getOptionKey());
		if (options != null) {
			eval.setKernel(parseIntOption(pAlgo, options));
			if (eval.getKernel() == null)
				eval.setKernel(1);
			eval.setCost(parseDoubleOption(pLambdaW, options));
			eval.setGamma(parseDoubleOption(pLambaU, options));
		}
		// parse predictions
		if (fileBaseName != null && fileBaseName.length() > 0) {
			List<List<Long>> listClassInfo = loadClassInfo(dataDir,
					fileBaseName + "class.txt");
			// process .output files
			if (listClassInfo != null) {
				parseSvmlinOutput(dataDir, outputDir, eval, fileBaseName,
						props, listClassInfo);
			}
		} else {
			log.warn("couldn't parse directory; kernel.label.base not defined. Dir: "
					+ outputDir);
		}

	}

	/**
	 * support multi-class classification
	 * 
	 * @param dataDir
	 * @param outputDir
	 * @param eval
	 * @param fileBaseName
	 * @param props
	 * @param predict
	 * @param listClassInfo
	 * @throws IOException
	 */
	private void parseSvmlinOutput(File dataDir, File outputDir,
			SVMClassifierEvaluation eval, String fileBaseName,
			Properties props, List<List<Long>> listClassInfo)
			throws IOException {
		Properties codeProps = FileUtil.loadProperties(
				dataDir.getAbsolutePath() + "/" + fileBaseName
						+ "code.properties", false);
		String[] codes = codeProps.getProperty("codes", "").split(",");
		SortedMap<Integer, double[]> codeToPredictionMap = new TreeMap<Integer, double[]>();
		if (codes.length == 0) {
			throw new IOException("invalid code.properties: " + fileBaseName);
		}
		int otherClassId = 0;
		if (codes.length == 1) {
			otherClassId = Integer
					.parseInt(codeProps.getProperty("classOther"));
		}
		for (String code : codes) {
			// determine class for given code
			String strClassId = codeProps.getProperty(code+".class");
			if (strClassId == null) {
				throw new IOException("invalid code.properties: "
						+ fileBaseName);
			}
			int classId = Integer.parseInt(strClassId);
			// read predictions for given class
			codeToPredictionMap.put(
					classId,
					readPredictions(outputDir.getAbsolutePath() + "/" + code
							+ ".outputs", listClassInfo.size()));
		}
		// iterate over predictions for each instance, figure out which class is
		// the winner
		int[] classPredictions = new int[listClassInfo.size()];
		for (int i = 0; i < listClassInfo.size(); i++) {
			if (otherClassId != 0) {
				Map.Entry<Integer, double[]> classToPred = codeToPredictionMap
						.entrySet().iterator().next();
				classPredictions[i] = classToPred.getValue()[i] > 0 ? classToPred
						.getKey() : otherClassId;
			} else {
				NavigableMap<Double, Integer> predToClassMap = new TreeMap<Double, Integer>();
				for (Map.Entry<Integer, double[]> classToPred : codeToPredictionMap
						.entrySet()) {
					predToClassMap.put(classToPred.getValue()[i],
							classToPred.getKey());
				}
				classPredictions[i] = predToClassMap.lastEntry().getValue();
			}
		}
		boolean storeUnlabeled = YES.equalsIgnoreCase(props.getProperty(
				ParseOption.STORE_UNLABELED.getOptionKey(),
				ParseOption.STORE_UNLABELED.getDefaultValue()));
		updateSemiSupervisedPredictions(eval, listClassInfo, storeUnlabeled,
				classPredictions);
	}

	/**
	 * read the predictions
	 * 
	 * @param predict
	 * @param expectedSize
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private double[] readPredictions(String predict, int expectedSize)
			throws FileNotFoundException, IOException {
		BufferedReader outputReader = null;
		try {
			double predictions[] = new double[expectedSize];
			int i = 0;
			String prediction = null;
			outputReader = new BufferedReader(new FileReader(predict));
			while ((prediction = outputReader.readLine()) != null) {
				if (i < expectedSize)
					predictions[i++] = (Double.parseDouble(prediction));
				else
					throw new IOException(predict
							+ ":  more predictions than expected");
			}
			if (i < expectedSize - 1)
				throw new IOException(predict
						+ ":  less predictions than expected");
			return predictions;
		} finally {
			if (outputReader != null) {
				try {
					outputReader.close();
				} catch (Exception ignore) {
				}
			}
		}
	}
}
