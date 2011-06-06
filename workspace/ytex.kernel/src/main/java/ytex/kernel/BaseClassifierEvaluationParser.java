package ytex.kernel;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ytex.kernel.model.ClassifierEvaluation;

/**
 * miscellaneous methods used for parsing various output types
 * 
 * @author vhacongarlav
 * 
 */
public abstract class BaseClassifierEvaluationParser implements
		ClassifierEvaluationParser {

	public static Pattern wsPattern = Pattern.compile("\\s|\\z");
	public static Pattern wsDotPattern = Pattern.compile("\\s|\\.|\\z");

	public static String extractFirstToken(String line, Pattern tokDelimPattern) {
		Matcher wsMatcher = tokDelimPattern.matcher(line);
		String token = null;
		if (wsMatcher.find() && wsMatcher.start() > 0) {
			token = line.substring(0, wsMatcher.start());
		}
		return token;
	}

	public List<Integer> parseInstanceIds(String instanceIdFile)
			throws IOException {
		BufferedReader instanceIdReader = null;
		List<Integer> instanceIds = new ArrayList<Integer>();
		try {
			instanceIdReader = new BufferedReader(
					new FileReader(instanceIdFile));
			String instanceId = null;
			while ((instanceId = instanceIdReader.readLine()) != null)
				instanceIds.add(Integer.parseInt(instanceId));
			return instanceIds;
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
		if (m.find())
			return Double.parseDouble(m.group(1));
		else
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
		if (strParam1 != null)
			eval.setParam1(Double.parseDouble(strParam1));
		eval.setParam2(props.getProperty("kernel.param2"));
	}

	public void parseDirectory(File dataDir, File outputDir,
			EnumSet<ParseOption> parseOptions) throws IOException {

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
		Properties kernelProps = new Properties();
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(outputDir
					.getPath()
					+ File.separator + "options.properties"));
			kernelProps.load(is);
		} catch (FileNotFoundException fe) {
			// do nothing - options not required
		} finally {
			if (is != null)
				is.close();
		}
		kernelProps.putAll(System.getProperties());
		return kernelProps;
	}
}
