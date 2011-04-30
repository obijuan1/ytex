package ytex.libsvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ytex.kernel.KernelContextHolder;
import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.ClassifierEvaluation;

public class LibSVMResultImporter {
	@SuppressWarnings("static-access")
	private static Options initOptions() {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("cvDir").hasArg()
				.withDescription("fold cross-validation results directory")
				.create("cvDir"));
		options.addOption(OptionBuilder.withArgName("model").hasArg()
				.withDescription("libsvm model file").create("model"));
		options.addOption(OptionBuilder.withArgName("predict").hasArg()
				.withDescription("libsvm output file").create("output"));
		options.addOption(OptionBuilder.withArgName("test").hasArg()
				.withDescription("libsvm input test data file").isRequired()
				.create("test"));
		options.addOption(OptionBuilder.withArgName("instanceId").hasArg()
				.withDescription("file with instance ids").create("instanceId"));
		options.addOption(OptionBuilder.withArgName("name").hasArg()
				.withDescription("name").isRequired().create("name"));
		options.addOption(OptionBuilder.withArgName("experiment").hasArg()
				.withDescription("experiment").create("experiment"));
		options.addOption(OptionBuilder.withArgName("options").hasArg()
				.withDescription("libsvm training options").create("options"));
		options.addOption(OptionBuilder.withArgName("fold").hasArg()
				.withDescription("fold").create("fold"));
		options.addOption(OptionBuilder.withArgName("label").hasArg()
				.withDescription("label").create("label"));
		options.addOption(OptionBuilder.withArgName("yes/no").hasArg()
				.withDescription("store instance evaluations, default no")
				.create("storeInstanceEval"));
		options.addOption(OptionBuilder.withArgName("yes/no").hasArg()
				.withDescription("store probabilities, default no")
				.create("storeProb"));
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
			try {
				CommandLine line = oparser.parse(options, args);
				if (line.hasOption("cvDir")) {
					importDirectory(line);
				} else {
					LibSVMParser lparser = new LibSVMParser();
					ClassifierEvaluation eval = lparser
							.parseClassifierEvaluation(line
									.getOptionValue("name"), line
									.getOptionValue("experiment"), line
									.getOptionValue("label"), line
									.getOptionValue("options"), line
									.getOptionValue("output"), line
									.getOptionValue("test"), line
									.getOptionValue("model"), line
									.getOptionValue("instanceId"), "yes"
									.equals(line.getOptionValue("storeProb",
											"no")));
					KernelContextHolder
							.getApplicationContext()
							.getBean(ClassifierEvaluationDao.class)
							.saveClassifierEvaluation(
									eval,
									"yes".equals(line.getOptionValue(
											"storeInstanceEval", "no")));
				}
			} catch (ParseException e) {
				printHelp(options);
				throw e;
			}
		}
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
	private static void importDirectory(CommandLine line) throws Exception {
		LibSVMParser lparser = new LibSVMParser();
		File cvDir = new File(line.getOptionValue("cvDir"));
		for (File resultDir : cvDir.listFiles()) {
			String model = resultDir + File.separator + "model.txt";
			String output = resultDir + File.separator + "predict.txt";
			String optionsFile = resultDir + File.separator
					+ "options.properties";
			if (checkFileRead(model) && checkFileRead(output)
					&& checkFileRead(optionsFile)) {
				String options = null;
				InputStream isOptions = null;
				try {
					isOptions = new FileInputStream(optionsFile);
					Properties props = new Properties();
					props.load(isOptions);
					options = props.getProperty("cv.eval.line");
				} finally {
					isOptions.close();
				}
				if (options != null) {
					ClassifierEvaluation eval = lparser
							.parseClassifierEvaluation(line
									.getOptionValue("name"), line
									.getOptionValue("experiment"), line
									.getOptionValue("label"), options, output,
									line.getOptionValue("test"), model, line
											.getOptionValue("instanceId"),
									"yes".equals(line.getOptionValue(
											"storeProb", "no")));
					KernelContextHolder
							.getApplicationContext()
							.getBean(ClassifierEvaluationDao.class)
							.saveClassifierEvaluation(
									eval,
									"yes".equals(line.getOptionValue(
											"storeInstanceEval", "no")));
				}
			}
		}
	}

	private static boolean checkFileRead(String file) {
		return (new File(file)).canRead();
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java ytex.libsvm.LibSVMResultImporter\n", options);
	}

}
