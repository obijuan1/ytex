package ytex.semil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.BaseClassifierEvaluationParser;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.ClassifierInstanceEvaluation;
import ytex.kernel.model.SemiLClassifierEvaluation;

/**
 * With semiL there is no test data set - just training data & unlabelled data.
 * Need the following files:
 * <ul>
 * <li>*.output - semil prediction output
 * <li>options.properties - options passed to semil (semil.distance), other
 * options (kernel.name, kernel.experiment)
 * <li>_test_data.txt - test class ids
 * <li>_train_id.txt - training label ids
 * <li>_test_id.txt - test label ids
 * </ul>
 * 
 * The semil output may contain the output of multiple runs. Will create a
 * classifier_eval record for each run. SemiL may change the label of labeled
 * data (soft label); currently the relabled instances will not be stored.
 * 
 * @author vhacongarlav
 * 
 */
public class SemiLEvaluationParser extends BaseClassifierEvaluationParser {
	private final static Log log = LogFactory
			.getLog(SemiLEvaluationParser.class);
	/**
	 * parse options
	 * 
	 * <pre>
	 *  gamma=10.000000 mu=0.500000 lambda=0.010100 hard_label=1 Laplacian=1 percentage of labeled points =0.000000
	 * </pre>
	 */
	public static Pattern pGamma = Pattern.compile("gamma=([\\d\\.\\-\\+e]+)");
	public static Pattern pMu = Pattern.compile("mu=([\\d\\.\\-\\+e]+)");
	public static Pattern pLambda = Pattern
			.compile("lambda=([\\d\\.\\-\\+e]+)");
	public static Pattern pLabel = Pattern.compile("hard_label=([01])");
	public static Pattern pLaplacian = Pattern.compile("Laplacian=([01])");
	public static Pattern pPercent = Pattern
			.compile("labeled points =([\\d\\.\\-\\+e]+)");

	private ClassifierEvaluationDao classifierEvaluationDao;

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	/**
	 * dummy
	 */
	@Override
	public ClassifierEvaluation parseClassifierEvaluation(String name,
			String experiment, String label, String options,
			String predictionFile, String trainInstanceFile, String modelFile,
			String trainInstanceIdFile, String testInstanceIdFile,
			boolean storeProbabilities) throws Exception {
		return null;
	}

	/**
	 * 
	 * @param fileBaseName
	 *            e.g. label1_run1_fold1
	 * @param dataDir
	 *            where train, test, id files are located
	 * @param outputDir
	 *            where classifier output is stored
	 */
	public void parseDirectory(File dataDir, File outputDir) throws IOException {
		Properties kernelProps = this.loadProps(outputDir);
		String tmpFileBaseName = kernelProps.getProperty(ParseOption.FOLD_BASE
				.getOptionKey(), ParseOption.FOLD_BASE.getDefaultValue());
		if (tmpFileBaseName.length() > 0)
			tmpFileBaseName = tmpFileBaseName + "_";
		final String fileBaseName = tmpFileBaseName;
		File testDataFile = new File(dataDir.getPath() + File.separator
				+ fileBaseName + "test_data.txt");
		File trainInstanceIdFile = new File(dataDir.getPath() + File.separator
				+ fileBaseName + "train_id.txt");
		File testInstanceIdFile = new File(dataDir.getPath() + File.separator
				+ fileBaseName + "test_id.txt");
		// get instance ids for train and test sets
		List<Integer> trainInstanceIdList = this
				.parseInstanceIds(trainInstanceIdFile.getPath());
		List<Integer> testInstanceIdList = this
				.parseInstanceIds(testInstanceIdFile.getPath());
		// get class ids for test set
		Map<Integer, Integer> testInstanceIdClassMap = getInstanceIdClass(
				testDataFile.getPath(), testInstanceIdList);
		for (File output : outputDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(fileBaseName) && name.endsWith(".output");
			}
		})) {
			parseSemiLOutput(fileBaseName, kernelProps, output,
					trainInstanceIdList, testInstanceIdClassMap);
		}
	}

	/**
	 * parse semil output file
	 * 
	 * @param fileBaseName
	 *            label1_run1_fold1
	 * @param kernelProps
	 *            from options.properties
	 * @param output
	 *            semil output file with predictions
	 * @param trainInstanceIdList
	 *            instance ids semil was trained on
	 * @param testInstanceIdClassMap
	 *            instance ids we're interested in for quantifying performance
	 * @param saveInstanceEval
	 *            should the instance-level evaluations be saved?
	 * @throws IOException
	 */
	private void parseSemiLOutput(String fileBaseName, Properties kernelProps,
			File output, List<Integer> trainInstanceIdList,
			Map<Integer, Integer> testInstanceIdClassMap) throws IOException {
		BufferedReader outputReader = null;
		try {
			outputReader = new BufferedReader(new FileReader(output));
			String optionsLine = null;
			String predictLine = null;
			while ((optionsLine = outputReader.readLine()) != null
					&& (predictLine = outputReader.readLine()) != null) {
				SemiLClassifierEvaluation ce = new SemiLClassifierEvaluation();
				// set label, fold, etc
				this.initClassifierEvaluation(fileBaseName, ce);
				// set name, experiment
				this.initClassifierEvaluationFromProperties(kernelProps, ce);
				// parse options
				parseOptions(ce, optionsLine, kernelProps);
				boolean storeInstanceEval = YES.equalsIgnoreCase(kernelProps
						.getProperty(ParseOption.STORE_INSTANCE_EVAL
								.getOptionKey(),
								ParseOption.STORE_INSTANCE_EVAL
										.getDefaultValue()));
				boolean storeUnlabeled = YES.equalsIgnoreCase(kernelProps
						.getProperty(
								ParseOption.STORE_UNLABELED.getOptionKey(),
								ParseOption.STORE_UNLABELED.getDefaultValue()));
				boolean storeIR = YES.equalsIgnoreCase(kernelProps.getProperty(
						ParseOption.STORE_IRSTATS.getOptionKey(),
						ParseOption.STORE_IRSTATS.getDefaultValue()));
				parsePredictedClasses(ce, predictLine, trainInstanceIdList,
						testInstanceIdClassMap, storeUnlabeled);
				// save the classifier evaluation
				this.getClassifierEvaluationDao().saveClassifierEvaluation(ce,
						storeInstanceEval || storeUnlabeled, storeIR, 0);
			}
		} finally {
			if (outputReader != null) {
				try {
					outputReader.close();
				} catch (Exception ignore) {
				}
			}
		}
	}

	/**
	 * parse class predictions for test instances out of semil output.
	 * 
	 * @param ce
	 *            evaluation to update
	 * @param predictLine
	 *            line with predictions
	 * @param trainInstanceIdList
	 *            instance ids corresponding to predictions
	 * @param testInstanceIdClassMap
	 *            test instance ids
	 * @param storeUnlabeled
	 *            should all predictions - not only for test instances be
	 *            stored?
	 */
	private void parsePredictedClasses(SemiLClassifierEvaluation ce,
			String predictLine, List<Integer> trainInstanceIdList,
			Map<Integer, Integer> testInstanceIdClassMap, boolean storeUnlabeled) {
		String classIds[] = predictLine.split("\\s");
		for (int i = 0; i < classIds.length; i++) {
			int instanceId = trainInstanceIdList.get(i);
			// if we are storing unlabeled instance ids, save this instance
			// evaluation
			// else only store it if this is a test instance id - save it
			if (storeUnlabeled
					|| testInstanceIdClassMap.containsKey(instanceId)) {
				ClassifierInstanceEvaluation cie = new ClassifierInstanceEvaluation();
				cie.setClassifierEvaluation(ce);
				cie.setInstanceId(instanceId);
				cie.setPredictedClassId(Integer.parseInt(classIds[i]));
				cie.setTargetClassId(testInstanceIdClassMap.get(instanceId));
				// add the instance eval to the parent
				ce.getClassifierInstanceEvaluations().put(instanceId, cie);
			}
		}
	}

	/**
	 * parse options out of file, into object. get the distance type from
	 * options.properties
	 * 
	 * <pre>
	 * gamma=10.000000  mu=0.500000  lambda=0.010100 hard_label=1 Laplacian=1 percentage of labeled points =0.000000 data_size=242
	 * </pre>
	 * 
	 * @param ce
	 * @param optionsLine
	 */
	private void parseOptions(SemiLClassifierEvaluation ce, String optionsLine,
			Properties kernelProps) {
		ce.setOptions(optionsLine);
		ce.setGamma(this.parseDoubleOption(pGamma, optionsLine));
		ce.setLambda(this.parseDoubleOption(pLambda, optionsLine));
		ce.setMu(this.parseDoubleOption(pMu, optionsLine));
		ce.setPercentLabeled(this.parseDoubleOption(pPercent, optionsLine));
		ce
				.setNormalizedLaplacian(this.parseIntOption(pLaplacian,
						optionsLine) == 1);
		ce.setSoftLabel(this.parseIntOption(pLabel, optionsLine) == 1);
		ce.setDistanceType(Integer.parseInt(kernelProps.getProperty(
				ParseOption.DISTANCE.getOptionKey(), ParseOption.DISTANCE
						.getDefaultValue())));
		ce.setAlgorithm("semiL");
	}

	/**
	 * 
	 * @param labelFile
	 *            contains class ids for each instance. first token of each line
	 *            is the class id.
	 * @param instanceIds
	 *            instance ids corresponding to lines
	 * @return
	 * @throws IOException
	 */
	Map<Integer, Integer> getInstanceIdClass(String labelFile,
			List<Integer> instanceIds) throws IOException {
		Map<Integer, Integer> mapInstanceIdClass = new HashMap<Integer, Integer>(
				instanceIds.size());
		BufferedReader instanceReader = null;
		try {
			instanceReader = new BufferedReader(new FileReader(labelFile));
			int nLine = 0;
			String instanceLine = null;
			while ((instanceLine = instanceReader.readLine()) != null) {
				mapInstanceIdClass.put(instanceIds.get(nLine), Integer
						.parseInt(extractFirstToken(instanceLine, wsPattern)));
				nLine++;
			}
		} finally {
			if (instanceReader != null) {
				try {
					instanceReader.close();
				} catch (Exception e) {
					log.error(labelFile, e);
				}
			}
		}
		return mapInstanceIdClass;
	}

	public static void main(String args[]) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: java "
					+ SemiLEvaluationParser.class.getName()
					+ "dataDir outputDir");
		} else {
			SemiLEvaluationParser parser = KernelContextHolder
					.getApplicationContext().getBean(
							SemiLEvaluationParser.class);
			parser.parseDirectory(new File(args[0]), new File(args[1]));
		}
	}
}
