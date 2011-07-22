package ytex.kernel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.CrossValidationFoldInstance;

/**
 * utility generates cv fold splits, stores in db.
 * 
 * @author vijay
 */
public class FoldGeneratorImpl implements FoldGenerator {
	private static final Log log = LogFactory.getLog(FoldGeneratorImpl.class);
	/**
	 * iterate through the labels, split instances into folds
	 * 
	 * @param mapClassToInstanceId
	 * @param nFolds
	 * @param nMinPerClass
	 * @param nSeed
	 * @return list with nFolds sets of line numbers corresponding to the folds
	 */
	private static List<Set<Long>> createFolds(
			Map<String, List<Long>> mapClassToInstanceId, int nFolds,
			int nMinPerClass, Random r) {
		List<Set<Long>> folds = new ArrayList<Set<Long>>(nFolds);
		Map<String, List<Set<Long>>> mapLabelFolds = new HashMap<String, List<Set<Long>>>();
		for (Map.Entry<String, List<Long>> classToInstanceId : mapClassToInstanceId
				.entrySet()) {
			List<Long> instanceIds = classToInstanceId.getValue();
			Collections.shuffle(instanceIds, r);
			List<Set<Long>> classFolds = new ArrayList<Set<Long>>(nFolds);
			int blockSize = instanceIds.size() / nFolds;
			for (int i = 0; i < nFolds; i++) {
				Set<Long> foldInstanceIds = new HashSet<Long>(blockSize);
				if (instanceIds.size() <= nMinPerClass) {
					// we don't have minPerClass for the given class
					// just add all of them to each fold
					foldInstanceIds.addAll(instanceIds);
				} else if (blockSize < nMinPerClass) {
					// too few of the given class - just randomly select
					// nMinPerClass
					double fraction = (double) nMinPerClass
							/ (double) instanceIds.size();
					// iterate through the list, start somewhere in the middle
					int instanceIdIndex = (int) (r.nextDouble() * instanceIds
							.size());
					while (foldInstanceIds.size() < nMinPerClass) {
						// go back to beginning of list if we hit the end
						if (instanceIdIndex >= instanceIds.size()) {
							instanceIdIndex = 0;
						}
						// randomly select this line
						if (r.nextDouble() <= fraction) {
							long instanceId = instanceIds.get(instanceIdIndex);
							foldInstanceIds.add(instanceId);
						}
						// go to next line
						instanceIdIndex++;
					}
				} else {
					int nStart = i * blockSize;
					int nEnd = (i == nFolds - 1) ? instanceIds.size() : nStart
							+ blockSize;
					for (int instanceIdIndex = nStart; instanceIdIndex < nEnd; instanceIdIndex++) {
						foldInstanceIds.add(instanceIds.get(instanceIdIndex));
					}
				}
				classFolds.add(foldInstanceIds);
			}
			mapLabelFolds.put(classToInstanceId.getKey(), classFolds);
		}
		for (int i = 0; i < nFolds; i++) {
			Set<Long> foldInstanceIds = new HashSet<Long>();
			for (List<Set<Long>> labelFold : mapLabelFolds.values()) {
				foldInstanceIds.addAll(labelFold.get(i));
			}
			folds.add(foldInstanceIds);
		}
		return folds;
	}
	@SuppressWarnings("static-access")
	public static void main(String args[]) throws ParseException, IOException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("prop")
				.hasArg()
				.withDescription(
						"property file with query to retrieve instance id - label - class triples")
				.create("prop"));
		// OptionGroup group = new OptionGroup();
		// group
		// .addOption(OptionBuilder
		// .withArgName("query")
		// .hasArg()
		// .withDescription(
		// "query to retrieve instance id - label - class triples")
		// .create("query"));
		// group
		// .addOption(OptionBuilder
		// .withArgName("prop")
		// .hasArg()
		// .withDescription(
		// "property file with query to retrieve instance id - label - class triples")
		// .create("prop"));
		// group.isRequired();
		// options.addOptionGroup(group);
		// options.addOption(OptionBuilder.withArgName("name").hasArg()
		// .isRequired().withDescription("name. required").create("name"));
		// options.addOption(OptionBuilder.withArgName("runs").hasArg()
		// .withDescription("number of runs, default 1").create("runs"));
		// options.addOption(OptionBuilder.withArgName("folds").hasArg()
		// .withDescription("number of folds, default 4").create("folds"));
		// options.addOption(OptionBuilder.withArgName("minPerClass").hasArg()
		// .withDescription("minimum instances per class, default 1")
		// .create("minPerClass"));
		// options.addOption(OptionBuilder.withArgName("rand").hasArg()
		// .withDescription(
		// "random number seed; default current time in millis")
		// .create("rand"));
		try {
			if (args.length == 0)
				printHelp(options);
			else {
				CommandLineParser parser = new GnuParser();
				CommandLine line = parser.parse(options, args);
				String propFile = line.getOptionValue("prop");
				Properties props = FileUtil.loadProperties(propFile, true);
				// Integer rand = line.hasOption("rand") ? Integer.parseInt(line
				// .getOptionValue("rand")) : null;
				// int runs = Integer.parseInt(line.getOptionValue("runs",
				// "1"));
				// int minPerClass = Integer.parseInt(line.getOptionValue(
				// "minPerClass", "1"));
				// int folds = Integer.parseInt(line.getOptionValue("folds",
				// "4"));
				String corpusName = props.getProperty("ytex.corpusName");
				String splitName = props.getProperty("ytex.splitName");
				String query = props.getProperty("instanceClassQuery");
				int folds = Integer.parseInt(props.getProperty("folds", "2"));
				int runs = Integer.parseInt(props.getProperty("runs", "5"));
				int minPerClass = Integer.parseInt(props.getProperty("minPerClass", "1"));
				Integer rand = props.containsKey("rand") ? Integer
						.parseInt(props.getProperty("rand")) : null;
				boolean argsOk = true;
				if(corpusName == null) {
					log.error("missing parameter: ytex.corpusName");
					argsOk = false;
				}
				if(query == null) {
					log.error("missing parameter: instanceClassQuery");
					argsOk = false;
				}
				if (!argsOk) {
					printHelp(options);
					System.exit(1);
				} else {
					KernelContextHolder
							.getApplicationContext()
							.getBean(FoldGenerator.class)
							.generateRuns(corpusName, splitName, query, folds,
									minPerClass, rand, runs);
				}
			}
		} catch (ParseException pe) {
			printHelp(options);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java ytex.kernel.FoldGeneratorImpl splits training data into mxn training/test sets for mxn-fold cross validation",
						options);
	}

	ClassifierEvaluationDao classifierEvaluationDao;

	KernelUtil kernelUtil;

	/**
	 * generate folds for a run
	 * 
	 * @param labels
	 * @param mapInstanceToClassLabel
	 * @param name
	 * @param splitName
	 * @param run
	 * @param query
	 * @param nFolds
	 * @param nMinPerClass
	 * @param r
	 */
	public void generateFolds(Set<String> labels, InstanceData instances,
			String corpusName, String splitName, int run, String query,
			int nFolds, int nMinPerClass, Random r) {
		for (String label : instances.getLabelToInstanceMap().keySet()) {
			// there should not be any runs/folds/train test split - just unpeel
			// until we get to the instance - class map
			SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>> runMap = instances
					.getLabelToInstanceMap().get(label);
			SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>> foldMap = runMap
					.values().iterator().next();
			SortedMap<Boolean, SortedMap<Long, String>> trainMap = foldMap
					.values().iterator().next();
			SortedMap<Long, String> mapInstanceIdToClass = trainMap.values()
					.iterator().next();
			// invert the mapInstanceIdToClass
			Map<String, List<Long>> mapClassToInstanceId = new TreeMap<String, List<Long>>();
			for (Map.Entry<Long, String> instance : mapInstanceIdToClass
					.entrySet()) {
				String className = instance.getValue();
				long instanceId = instance.getKey();
				List<Long> classInstanceIds = mapClassToInstanceId
						.get(className);
				if (classInstanceIds == null) {
					classInstanceIds = new ArrayList<Long>();
					mapClassToInstanceId.put(className, classInstanceIds);
				}
				classInstanceIds.add(instanceId);
			}
			// stratified split into folds
			List<Set<Long>> folds = createFolds(mapClassToInstanceId, nFolds,
					nMinPerClass, r);
			// insert the folds
			insertFolds(folds, corpusName, splitName, label, run);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.FoldGenerator#generateRuns(java.lang.String,
	 * java.lang.String, int, int, java.lang.Integer, int)
	 */
	@Override
	public void generateRuns(String corpusName, String splitName, String query,
			int nFolds, int nMinPerClass, Integer nSeed, int nRuns) {
		Random r = new Random(nSeed != null ? nSeed
				: System.currentTimeMillis());
		SortedSet<String> labels = new TreeSet<String>();
		InstanceData instances = kernelUtil.loadInstances(query);
		this.getClassifierEvaluationDao().deleteCrossValidationFoldByName(
				corpusName, splitName);
		for (int run = 1; run <= nRuns; run++) {
			generateFolds(labels, instances, corpusName, splitName, run, query,
					nFolds, nMinPerClass, r);
		}
	}
	
	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public KernelUtil getKernelUtil() {
		return kernelUtil;
	}

	/**
	 * insert the folds into the database
	 * 
	 * @param folds
	 * @param corpusName
	 * @param run
	 */
	private void insertFolds(List<Set<Long>> folds, String corpusName,
			String splitName, String label, int run) {
		// iterate over fold numbers
		for (int foldNum = 1; foldNum <= folds.size(); foldNum++) {
			Set<CrossValidationFoldInstance> instanceIds = new HashSet<CrossValidationFoldInstance>();
			// iterate over instances in each fold
			for (int trainFoldNum = 1; trainFoldNum <= folds.size(); trainFoldNum++) {
				// add the instance, set the train flag
				for (long instanceId : folds.get(trainFoldNum - 1))
					instanceIds.add(new CrossValidationFoldInstance(instanceId,
							trainFoldNum != foldNum));
			}
			classifierEvaluationDao.saveFold(new CrossValidationFold(
					corpusName, splitName, label, run, foldNum, instanceIds));
			// insert test set
			// classifierEvaluationDao.saveFold(new CrossValidationFold(name,
			// label, run, foldNum, false, folds.get(foldNum - 1)));
			// insert training set
			// Set<Integer> trainInstances = new TreeSet<Integer>();
			// for (int trainFoldNum = 1; trainFoldNum <= folds.size();
			// trainFoldNum++) {
			// if (trainFoldNum != foldNum)
			// trainInstances.addAll(folds.get(trainFoldNum - 1));
			// }
			// classifierEvaluationDao.saveFold(new CrossValidationFold(name,
			// label, run, foldNum, true, trainInstances));
		}
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public void setKernelUtil(KernelUtil kernelUtil) {
		this.kernelUtil = kernelUtil;
	}

}
