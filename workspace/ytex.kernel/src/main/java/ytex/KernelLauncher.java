package ytex;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import ytex.kernel.evaluator.CorpusKernelEvaluator;
import ytex.kernel.tree.InstanceTreeBuilder;
import ytex.kernel.tree.Node;
import ytex.kernel.tree.TreeMappingInfo;

public class KernelLauncher {
	private static Options initOptions() {
		Option oStoreInstanceMap = OptionBuilder
				.withArgName("instanceMap.obj")
				.hasArg()
				.withDescription(
						"store the instanceMap.  Use prior to running the kernel evaluations in parallel.")
				.create("storeInstanceMap");
		Option oEvaluateKernel = OptionBuilder
				.withDescription(
						"evaluate kernel specified in application context on the instances. If instanceMap is specified, load instance from file system, else from db.")
				.create("evalKernel");
		Option oLoadInstanceMap = OptionBuilder
				.withArgName("instanceMap.obj")
				.hasArg()
				.withDescription(
						"load instanceMap from file system instead of from db.  Use after storing instance map.")
				.create("loadInstanceMap");
		Option oEvalMod = OptionBuilder
				.withDescription(
						"for parallelization, split the instances into mod slices")
				.hasArg().create("mod");
		Option oEvalSlice = OptionBuilder
				.withDescription(
						"for parallelization, parameter that determines which slice we work on")
				.hasArg().create("slice");
		Option oBeanref = OptionBuilder
				.withArgName("classpath*:simSvcBeanRefContext.xml")
				.hasArg()
				.withDescription(
						"use specified beanRefContext.xml, default classpath*:simSvcBeanRefContext.xml")
				.create("beanref");
		Option oAppctx = OptionBuilder
				.withArgName("kernelApplicationContext")
				.hasArg()
				.withDescription(
						"use specified applicationContext, default kernelApplicationContext")
				.create("appctx");
		Option oBeans = OptionBuilder
				.withArgName("beans-corpus.xml")
				.hasArg()
				.withDescription(
						"use specified beans.xml, no default.  This file is typically required.")
				.create("beans");
		Option oHelp = new Option("help", "print this message");
		Options options = new Options();
		options.addOption(oStoreInstanceMap);
		options.addOption(oEvaluateKernel);
		options.addOption(oLoadInstanceMap);
		options.addOption(oEvalMod);
		options.addOption(oEvalSlice);
		options.addOption(oBeanref);
		options.addOption(oAppctx);
		options.addOption(oBeans);
		options.addOption(oHelp);
		return options;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java ytex.kernel.evaluator.CorpusKernelEvaluatorImpl\n Main Options: -storeInstanceMap or -evalKernel",
						options);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Options options = initOptions();

		if (args.length == 0) {
			printHelp(options);
		} else {
			CommandLineParser parser = new GnuParser();
			try {
				// parse the command line arguments
				CommandLine line = parser.parse(options, args);
				String storeInstanceMap = line
						.getOptionValue("storeInstanceMap");
				boolean evalKernel = line.hasOption("evalKernel");
				if ((evalKernel && storeInstanceMap != null)
						|| (!evalKernel && storeInstanceMap == null)) {
					System.out
							.println("specify either -evalKernel or -storeInstanceMap");
					printHelp(options);
				} else {
					// parse the command line arguments
					String beanRefContext = line.getOptionValue("beanref",
							"classpath*:simSvcBeanRefContext.xml");
					String contextName = line.getOptionValue("appctx",
							"kernelApplicationContext");
					String beans = line.getOptionValue("beans");
					ApplicationContext appCtx = (ApplicationContext) ContextSingletonBeanFactoryLocator
							.getInstance(beanRefContext)
							.useBeanFactory(contextName).getFactory();
					ApplicationContext appCtxSource = appCtx;
					if (beans != null) {
						appCtxSource = new FileSystemXmlApplicationContext(
								new String[] { beans }, appCtx);
					}
					if (storeInstanceMap != null) {
						storeInstanceMap(appCtxSource, storeInstanceMap, line);
					} else if (evalKernel) {
						evalKernel(appCtxSource, line);
					}
				}
			} catch (ParseException e) {
				printHelp(options);
				throw e;
			}
		}
	}

	private static void evalKernel(ApplicationContext appCtxSource,
			CommandLine line) throws Exception {
		InstanceTreeBuilder builder = appCtxSource.getBean(
				"instanceTreeBuilder", InstanceTreeBuilder.class);
		CorpusKernelEvaluator corpusEvaluator = appCtxSource.getBean(
				"corpusKernelEvaluator", CorpusKernelEvaluator.class);
		String loadInstanceMap = line.getOptionValue("loadInstanceMap");
		String strMod = line.getOptionValue("mod");
		String strSlice = line.getOptionValue("slice");
		int nMod = strMod != null ? Integer.parseInt(strMod) : 0;
		int nSlice = strMod != null ? Integer.parseInt(strSlice) : 0;
		Map<Integer, Node> instanceMap = null;
		if (loadInstanceMap != null) {
			instanceMap = builder.loadInstanceTrees(loadInstanceMap);
		} else {
			instanceMap = builder.loadInstanceTrees(appCtxSource.getBean(
					"treeMappingInfo", TreeMappingInfo.class));
		}
		corpusEvaluator.evaluateKernelOnCorpus(instanceMap, nMod, nSlice);
	}

	private static void storeInstanceMap(ApplicationContext appCtxSource,
			String storeInstanceMap, CommandLine line) throws Exception {
		InstanceTreeBuilder builder = appCtxSource.getBean(
				"instanceTreeBuilder", InstanceTreeBuilder.class);
		builder.serializeInstanceTrees(
				appCtxSource.getBean("treeMappingInfo", TreeMappingInfo.class),
				storeInstanceMap);
	}
}
