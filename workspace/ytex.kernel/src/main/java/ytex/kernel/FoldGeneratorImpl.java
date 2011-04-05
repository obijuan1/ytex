package ytex.kernel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.CrossValidationFoldInstance;
import ytex.libsvm.LibSVMUtil;

/**
 * utility generates cv fold splits, stores in db.
 * 
 * @author vijay
 */
public class FoldGeneratorImpl implements FoldGenerator {
	LibSVMUtil libsvmUtil;
	ClassifierEvaluationDao classifierEvaluationDao;

	public LibSVMUtil getLibsvmUtil() {
		return libsvmUtil;
	}

	public void setLibsvmUtil(LibSVMUtil libsvmUtil) {
		this.libsvmUtil = libsvmUtil;
	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.FoldGenerator#generateRuns(java.lang.String,
	 * java.lang.String, int, int, java.lang.Integer, int)
	 */
	@Override
	public void generateRuns(String name, String query, int nFolds,
			int nMinPerClass, Integer nSeed, int nRuns) {
		Random r = new Random(nSeed != null ? nSeed
				: System.currentTimeMillis());
		SortedSet<String> labels = new TreeSet<String>();
		SortedMap<Integer, Map<String, Integer>> mapInstanceToClassLabel = libsvmUtil
				.loadClassLabels(query, labels);
		this.getClassifierEvaluationDao().deleteCrossValidationFoldByName(name);
		for (int run = 1; run <= nRuns; run++) {
			generateFolds(labels, mapInstanceToClassLabel, name, run, query,
					nFolds, nMinPerClass, r);
		}
	}

	/**
	 * generate folds for a run
	 * 
	 * @param labels
	 * @param mapInstanceToClassLabel
	 * @param name
	 * @param run
	 * @param query
	 * @param nFolds
	 * @param nMinPerClass
	 * @param r
	 */
	public void generateFolds(Set<String> labels,
			Map<Integer, Map<String, Integer>> mapInstanceToClassLabel,
			String name, int run, String query, int nFolds, int nMinPerClass,
			Random r) {
		for (String label : labels) {
			Map<Integer, List<Integer>> mapClassToInstanceId = new TreeMap<Integer, List<Integer>>();
			for (Map.Entry<Integer, Map<String, Integer>> instance : mapInstanceToClassLabel
					.entrySet()) {
				if (instance.getValue().containsKey(label)) {
					// instance has a class assignment for the given label - add
					// it to mapclassToInstanceId
					List<Integer> classInstanceIds = mapClassToInstanceId
							.get(instance.getValue().get(label));
					if (classInstanceIds == null) {
						// allocate array for instance's class because it hasn't
						// been added yet
						classInstanceIds = new ArrayList<Integer>();
						mapClassToInstanceId.put(
								instance.getValue().get(label),
								classInstanceIds);
					}
					// add instance id
					classInstanceIds.add(instance.getKey());
				}
			}
			List<Set<Integer>> folds = createFolds(mapClassToInstanceId,
					nFolds, nMinPerClass, r);
			insertFolds(folds, name, label, run);
		}
	}

	/**
	 * insert the folds into the database
	 * 
	 * @param folds
	 * @param name
	 * @param run
	 */
	private void insertFolds(List<Set<Integer>> folds, String name,
			String label, int run) {
		// iterate over fold numbers
		for (int foldNum = 1; foldNum <= folds.size(); foldNum++) {
			Set<CrossValidationFoldInstance> instanceIds = new HashSet<CrossValidationFoldInstance>();
			// iterate over instances in each fold
			for (int trainFoldNum = 1; trainFoldNum <= folds.size(); trainFoldNum++) {
				// add the instance, set the train flag
				for (int instanceId : folds.get(trainFoldNum - 1))
					instanceIds.add(new CrossValidationFoldInstance(instanceId,
							trainFoldNum != foldNum));
			}
			classifierEvaluationDao.saveFold(new CrossValidationFold(name,
					label, run, foldNum, instanceIds));
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

	/**
	 * iterate through the labels, split instances into folds
	 * 
	 * @param mapClassToInstanceId
	 * @param nFolds
	 * @param nMinPerClass
	 * @param nSeed
	 * @return list with nFolds sets of line numbers corresponding to the folds
	 */
	private static List<Set<Integer>> createFolds(
			Map<Integer, List<Integer>> mapClassToInstanceId, int nFolds,
			int nMinPerClass, Random r) {
		List<Set<Integer>> folds = new ArrayList<Set<Integer>>(nFolds);
		Map<Integer, List<Set<Integer>>> mapLabelFolds = new HashMap<Integer, List<Set<Integer>>>();
		for (Map.Entry<Integer, List<Integer>> classToInstanceId : mapClassToInstanceId
				.entrySet()) {
			List<Integer> instanceIds = classToInstanceId.getValue();
			Collections.shuffle(instanceIds, r);
			List<Set<Integer>> classFolds = new ArrayList<Set<Integer>>(nFolds);
			int blockSize = instanceIds.size() / nFolds;
			for (int i = 0; i < nFolds; i++) {
				Set<Integer> foldInstanceIds = new HashSet<Integer>(blockSize);
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
							int nLineNum = instanceIds.get(instanceIdIndex);
							foldInstanceIds.add(nLineNum);
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
			Set<Integer> foldLineNums = new HashSet<Integer>();
			for (List<Set<Integer>> labelFold : mapLabelFolds.values()) {
				foldLineNums.addAll(labelFold.get(i));
			}
			folds.add(foldLineNums);
		}
		return folds;
	}

	@SuppressWarnings("static-access")
	public static void main(String args[]) throws ParseException, IOException {
		Options options = new Options();
		OptionGroup group = new OptionGroup();
		group.addOption(OptionBuilder
				.withArgName("query")
				.hasArg()
				.withDescription(
						"query to retrieve instance id - label - class triples")
				.create("query"));
		group.addOption(OptionBuilder
				.withArgName("prop")
				.hasArg()
				.withDescription(
						"property file with query to retrieve instance id - label - class triples")
				.create("prop"));
		group.isRequired();
		options.addOptionGroup(group);
		options.addOption(OptionBuilder.withArgName("name").hasArg()
				.isRequired().withDescription("name. required").create("name"));
		options.addOption(OptionBuilder.withArgName("runs").hasArg()
				.withDescription("number of runs, default 1").create("runs"));
		options.addOption(OptionBuilder.withArgName("folds").hasArg()
				.withDescription("number of folds, default 4").create("folds"));
		options.addOption(OptionBuilder.withArgName("minPerClass").hasArg()
				.withDescription("minimum instances per class, default 1")
				.create("minPerClass"));
		options.addOption(OptionBuilder
				.withArgName("rand")
				.hasArg()
				.withDescription(
						"random number seed; default current time in millis")
				.create("rand"));
		try {
			if (args.length == 0)
				printHelp(options);
			else {
				CommandLineParser parser = new GnuParser();
				CommandLine line = parser.parse(options, args);
				Integer rand = line.hasOption("rand") ? Integer.parseInt(line
						.getOptionValue("rand")) : null;
				int runs = Integer.parseInt(line.getOptionValue("runs", "1"));
				int minPerClass = Integer.parseInt(line.getOptionValue(
						"minPerClass", "1"));
				int folds = Integer.parseInt(line.getOptionValue("folds", "4"));
				String name = line.getOptionValue("name");
				String query;
				if (line.hasOption("query")) {
					query = line.getOptionValue("query");
				} else {
					String propFile = line.getOptionValue("prop");
					InputStream is = null;
					try {
						is = new FileInputStream(propFile);
						Properties props = new Properties();
						if (propFile.endsWith(".xml"))
							props.loadFromXML(is);
						else
							props.load(is);
						query = props.getProperty("train.instance.query");
					} finally {
						if (is != null)
							is.close();
					}
				}
				if (query != null && name != null) {
					KernelContextHolder
							.getApplicationContext()
							.getBean(FoldGenerator.class)
							.generateRuns(name, query, folds, minPerClass,
									rand, runs);
				} else {
					printHelp(options);
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
						"java ytex.kernel.FoldGenerator splits training data into mxn training/test sets for mxn-fold cross validation",
						options);
	}

}
