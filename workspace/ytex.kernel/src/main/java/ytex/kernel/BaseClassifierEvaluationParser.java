package ytex.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.ClassifierEvaluation;

/**
 * miscellaneous methods used for parsing various output types
 * 
 * @author vhacongarlav
 * 
 */
public abstract class BaseClassifierEvaluationParser implements
		ClassifierEvaluationParser {
	private static final Log log = LogFactory
			.getLog(BaseClassifierEvaluationParser.class);

	public static Pattern wsPattern = Pattern.compile("\\s|\\z");
	public static Pattern wsDotPattern = Pattern.compile("\\s|\\.|\\z");

	private ClassifierEvaluationDao classifierEvaluationDao;

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public static String extractFirstToken(String line, Pattern tokDelimPattern) {
		Matcher wsMatcher = tokDelimPattern.matcher(line);
		String token = null;
		if (wsMatcher.find() && wsMatcher.start() > 0) {
			token = line.substring(0, wsMatcher.start());
		}
		return token;
	}

	public List<Long> parseInstanceIds(String instanceIdFile)
			throws IOException {
		BufferedReader instanceIdReader = null;
		List<Long> instanceIds = new ArrayList<Long>();
		try {
			instanceIdReader = new BufferedReader(
					new FileReader(instanceIdFile));
			String instanceId = null;
			while ((instanceId = instanceIdReader.readLine()) != null)
				instanceIds.add(Long.parseLong(instanceId));
			return instanceIds;
		} catch(FileNotFoundException e) {
			log.warn(instanceIdFile + " not available, instance_ids will not be stored");
			return null;
		} finally {
			if (instanceIdReader != null)
				instanceIdReader.close();
		}
	}

	/**
	 * parse a number out of the libsvm command line that matches the specified
	 * pattern.
	 * 
	 * @param pCost
	 * @param options
	 * @return null if option not present
	 */
	protected Double parseDoubleOption(Pattern pCost, String options) {
		Matcher m = pCost.matcher(options);
		if (m.find()) {
			String toParse = m.group(1);
			try {
				return Double.parseDouble(toParse);
			} catch (NumberFormatException nfe) {
				log.warn("could not parse: " + toParse, nfe);
			}
		}
		return null;
	}

	/**
	 * 
	 * parse a number out of the libsvm command line that matches the specified
	 * pattern.
	 * 
	 * @param pKernel
	 * @param options
	 * @return null if option not present
	 */
	protected Integer parseIntOption(Pattern pKernel, String options) {
		Matcher m = pKernel.matcher(options);
		if (m.find())
			return Integer.parseInt(m.group(1));
		else
			return null;
	}

	protected void initClassifierEvaluation(String instanceIdFile,
			ClassifierEvaluation eval) {
		eval.setFold(FileUtil.parseFoldFromFileName(instanceIdFile));
		eval.setRun(FileUtil.parseRunFromFileName(instanceIdFile));
		eval.setLabel(FileUtil.parseLabelFromFileName(instanceIdFile));
	}

	protected void initClassifierEvaluationFromProperties(Properties props,
			ClassifierEvaluation eval) {
		eval.setName(props.getProperty("kernel.name"));
		eval.setExperiment(props.getProperty("kernel.experiment"));
		String strParam1 = props.getProperty("kernel.param1");
		if (strParam1 != null && strParam1.length() > 0)
			eval.setParam1(Double.parseDouble(strParam1));
		eval.setParam2(props.getProperty("kernel.param2"));
		eval.setOptions(props.getProperty(ParseOption.EVAL_LINE.getOptionKey()));
	}

	/**
	 * load properties from <tt>outputDir/options.properties</tt>. returns empty
	 * properties if the file does not exist
	 * 
	 * @param outputDir
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Properties loadProps(File outputDir) throws FileNotFoundException,
			IOException {
		return FileUtil.loadProperties(outputDir.getPath() + File.separator
				+ "options.properties", true);
	}

	protected boolean checkFileRead(String file) {
		return (new File(file)).canRead();
	}

	protected String getFileBaseName(Properties kernelProps) {
		return kernelProps.getProperty(
				ParseOption.DATA_BASENAME.getOptionKey(),
				ParseOption.DATA_BASENAME.getDefaultValue());
	}

	protected void storeSemiSupervised(Properties kernelProps,
			ClassifierEvaluation ce) {
		boolean storeInstanceEval = YES.equalsIgnoreCase(kernelProps
				.getProperty(ParseOption.STORE_INSTANCE_EVAL.getOptionKey(),
						ParseOption.STORE_INSTANCE_EVAL.getDefaultValue()));
		boolean storeUnlabeled = YES.equalsIgnoreCase(kernelProps.getProperty(
				ParseOption.STORE_UNLABELED.getOptionKey(),
				ParseOption.STORE_UNLABELED.getDefaultValue()));
		boolean storeIR = YES.equalsIgnoreCase(kernelProps.getProperty(
				ParseOption.STORE_IRSTATS.getOptionKey(),
				ParseOption.STORE_IRSTATS.getDefaultValue()));
		// save the classifier evaluation
		this.getClassifierEvaluationDao().saveClassifierEvaluation(ce,
				storeInstanceEval || storeUnlabeled, storeIR, 0);
	}
}
