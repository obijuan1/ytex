package ytex.libsvm;

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
	private static Options initOptions() {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("model").hasArg()
				.withDescription("libsvm model file").isRequired().create(
						"model"));
		options.addOption(OptionBuilder.withArgName("predict").hasArg()
				.withDescription("libsvm output file").isRequired().create(
						"output"));
		options.addOption(OptionBuilder.withArgName("test").hasArg()
				.withDescription("libsvm input test data file").isRequired()
				.create("test"));
		options.addOption(OptionBuilder.withArgName("instanceId").hasArg()
				.withDescription("file with instance ids").create(
						"instanceId"));
		options.addOption(OptionBuilder.withArgName("name").hasArg()
				.withDescription("name").isRequired().create("name"));
		options.addOption(OptionBuilder.withArgName("options").hasArg()
				.withDescription("libsvm training options").create("options"));
		options.addOption(OptionBuilder.withArgName("fold").hasArg()
				.withDescription("fold").create("fold"));
		options.addOption(OptionBuilder.withArgName("label").hasArg()
				.withDescription("label").create("label"));
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
				LibSVMParser lparser = new LibSVMParser();
				ClassifierEvaluation eval = lparser.parseClassifierEvaluation(line.getOptionValue("name"),
						line.getOptionValue("label"), line
								.getOptionValue("options"), line
								.getOptionValue("fold"), line
								.getOptionValue("output"), line
								.getOptionValue("test"), line
								.getOptionValue("model"), line
								.getOptionValue("instanceId"));
				KernelContextHolder.getApplicationContext().getBean(ClassifierEvaluationDao.class).saveClassifierEvaluation(eval);
			} catch (ParseException e) {
				printHelp(options);
				throw e;
			}
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java ytex.libsvm.LibSVMResultImporter\n",
						options);
	}

}
