package ytex.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.ClassifierEvaluationParser.ParseOption;
import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.ClassifierEvaluation;

public class ClassifierEvaluationImporter {
	private static final Log log = LogFactory
			.getLog(ClassifierEvaluationImporter.class);

	private Map<String, ClassifierEvaluationParser> nameToParserMap;

	public Map<String, ClassifierEvaluationParser> getNameToParserMap() {
		return nameToParserMap;
	}

	public void setNameToParserMap(
			Map<String, ClassifierEvaluationParser> nameToParserMap) {
		this.nameToParserMap = nameToParserMap;
	}

	@SuppressWarnings("static-access")
	private static Options initOptions() {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("cvDir").hasArg()
				.withDescription("fold cross-validation results directory")
				.create("cvDir"));
		options.addOption(OptionBuilder.withArgName("model").hasArg()
				.withDescription("svm model file").create("model"));
		options.addOption(OptionBuilder.withArgName("output").hasArg()
				.withDescription("svm training output").create("output"));
		options
				.addOption(OptionBuilder.withArgName("predict").hasArg()
						.withDescription("svm test predictions file").create(
								"predict"));
		options.addOption(OptionBuilder.withArgName("test").hasArg()
				.withDescription("input test data file").isRequired().create(
						"test"));
		options
				.addOption(OptionBuilder.withArgName("instanceId").hasArg()
						.withDescription("file with instance ids").create(
								"instanceId"));
		options.addOption(OptionBuilder.withArgName("name").hasArg()
				.withDescription("name").isRequired().create("name"));
		options.addOption(OptionBuilder.withArgName("experiment").hasArg()
				.withDescription("experiment").create("experiment"));
		options.addOption(OptionBuilder.withArgName("options").hasArg()
				.withDescription("svm training options").create("options"));
		options.addOption(OptionBuilder.withArgName("fold").hasArg()
				.withDescription("fold").create("fold"));
		options.addOption(OptionBuilder.withArgName("label").hasArg()
				.withDescription("label").create("label"));
		options.addOption(OptionBuilder.withArgName("type").hasArg()
				.withDescription("libsvm (default) or svmlight or semil")
				.create("type"));
		options.addOption(OptionBuilder.withArgName("yes/no").hasArg()
				.withDescription("store instance evaluations, default no")
				.create("storeInstanceEval"));
		options.addOption(OptionBuilder.withArgName("yes/no").hasArg()
				.withDescription("store probabilities, default no").create(
						"storeProb"));
		options
				.addOption(OptionBuilder
						.withArgName("yes/no")
						.hasArg()
						.withDescription(
								"store instance evaluations for unlabeled data (relevant only for transductive learning), default no")
						.create("storeUnlabeled"));
		return options;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Options options = initOptions();
		if (args.length == 0) {
			printHelp(options);
		} else {
			CommandLineParser oparser = new GnuParser();
			ClassifierEvaluationImporter importer = KernelContextHolder
					.getApplicationContext().getBean(
							ClassifierEvaluationImporter.class);
			ClassifierEvaluationDao dao = KernelContextHolder
					.getApplicationContext().getBean(
							ClassifierEvaluationDao.class);
			try {
				CommandLine line = oparser.parse(options, args);
				if (line.hasOption("cvDir")) {
					importer.importDirectory(line);
				} else {
					ClassifierEvaluationParser lparser = importer
							.getParser(line);
					ClassifierEvaluation eval = lparser
							.parseClassifierEvaluation(line
									.getOptionValue("name"), line
									.getOptionValue("experiment"), line
									.getOptionValue("label"), line
									.getOptionValue("options"), line
									.getOptionValue("predict"), line
									.getOptionValue("test"), line
									.getOptionValue("model"), line
									.getOptionValue("instanceId"), line
									.getOptionValue("output"), "yes"
									.equals(line.getOptionValue("storeProb",
											"no")));
					dao.saveClassifierEvaluation(eval, "yes".equals(line
							.getOptionValue("storeInstanceEval", "no")));
				}
			} catch (ParseException e) {
				printHelp(options);
				throw e;
			}
		}
	}

	private ClassifierEvaluationParser getParser(CommandLine line) {
		String type = line.getOptionValue("type", "libsvm");
		return this.nameToParserMap.get(type);
	}

	/**
	 * Expect directory with subdirectories for each evaluation. Subdirectories
	 * must contain following in order for results to be processed:
	 * <ul>
	 * <li>
	 * model.txt: libsvm model trained on training set
	 * <li>predict.txt: libsvm predictions on test set
	 * <li>options.properties: libsvm command line options
	 * </ul>
	 * 
	 * @param line
	 * @throws Exception
	 */
	private void importDirectory(CommandLine line) throws Exception {
		ClassifierEvaluationParser lparser = getParser(line);
		boolean storeInstanceEval = "yes".equals(line.getOptionValue(
				"storeInstanceEval", "no"));
		File cvDir = new File(line.getOptionValue("cvDir"));
		String type = line.getOptionValue("type", "libsvm");
		Set<ParseOption> parseOptionsTmp = new HashSet<ParseOption>();
		if (storeInstanceEval)
			parseOptionsTmp.add(ParseOption.STORE_INSTANCE_EVAL);
		if ("yes".equals(line.getOptionValue("storeProb", "no")))
			parseOptionsTmp.add(ParseOption.STORE_PROBABILITIES);
		if ("yes".equals(line.getOptionValue("storeUnlabeled", "no")))
			parseOptionsTmp.add(ParseOption.STORE_UNLABELED);
		EnumSet<ParseOption> parseOptions = parseOptionsTmp.isEmpty() ? EnumSet
				.noneOf(ParseOption.class) : EnumSet.copyOf(parseOptionsTmp);
		for (File resultDir : cvDir.listFiles()) {
			String output = resultDir + File.separator + "output.txt";
			String model = resultDir + File.separator + "model.txt";
			String predict = resultDir + File.separator + "predict.txt";
			String optionsFile = resultDir + File.separator
					+ "options.properties";
			if ("semil".equals(type) && checkFileRead(optionsFile)) {
				lparser.parseDirectory(new File("."), resultDir, parseOptions);
			} else if (("libsvm".equals(type) || "svmlight".equals(type))
					&& checkFileRead(model) && checkFileRead(predict)
					&& checkFileRead(optionsFile)) {
				String options = null;
				Double param1 = null;
				String param2 = null;
				InputStream isOptions = null;
				try {
					isOptions = new FileInputStream(optionsFile);
					Properties props = new Properties();
					props.load(isOptions);
					options = props.getProperty("kernel.eval.line");
					String strParam1 = props.getProperty("kernel.param1", null);
					if (strParam1 != null) {
						try {
							param1 = Double.parseDouble(strParam1);
						} catch (Exception e) {
							log.warn("error parasing param1: " + strParam1, e);
						}
					}
					param2 = props.getProperty("kernel.param2");
				} finally {
					isOptions.close();
				}
				if (options != null) {
					try {
						ClassifierEvaluation eval = lparser
								.parseClassifierEvaluation(line
										.getOptionValue("name"), line
										.getOptionValue("experiment"), line
										.getOptionValue("label"), options,
										predict, line.getOptionValue("test"),
										model, line
												.getOptionValue("instanceId"),
										output, "yes".equals(line
												.getOptionValue("storeProb",
														"no")));
						eval.setParam1(param1);
						eval.setParam2(param2);
						KernelContextHolder.getApplicationContext().getBean(
								ClassifierEvaluationDao.class)
								.saveClassifierEvaluation(eval,
										storeInstanceEval);
					} catch (Exception e) {
						// continue processing - don't give up because of one
						// bad file
						log.warn("error importing results, resultDir="
								+ resultDir.getAbsolutePath(), e);
					}
				}
			}
		}
	}

	private static boolean checkFileRead(String file) {
		return (new File(file)).canRead();
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java ytex.libsvm.ClassifierEvaluationImporter\n",
				options);
	}

}
