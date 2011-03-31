package ytex.libsvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CreateStratifiedCVFolds {
	@SuppressWarnings("static-access")
	private static Options initOptions() {
		Option data = OptionBuilder.withArgName("data").hasArg()
				.withDescription("input data").isRequired().create("data");
		Option instanceId = OptionBuilder.withArgName("instanceId").hasArg()
				.withDescription("file with instance ids").create("instanceId");
		Option outdir = OptionBuilder
				.withArgName("outdir")
				.hasArg()
				.withDescription(
						"output directory, default same directory as input data")
				.create("outdir");
		Option folds = OptionBuilder.withArgName("folds").hasArg()
				.withDescription("# folds, default 10").create("folds");
		Option seed = OptionBuilder.withArgName("seed").hasArg()
				.withDescription("random seed").create("seed");
		Option minClass = OptionBuilder
				.withArgName("minClass")
				.hasArg()
				.withDescription(
						"min instances per class.  for classes w/ few members, will result in overlapping folds")
				.create("minClass");
		Options options = new Options();
		options.addOption(data);
		options.addOption(instanceId);
		options.addOption(outdir);
		options.addOption(folds);
		options.addOption(seed);
		options.addOption(minClass);
		return options;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java ytex.libsvm.CreateStratifiedCVFolds splits svm training data into n training/test sets for n-fold cross validation",
						options);
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {
		Options options = initOptions();
		CommandLineParser parser = new GnuParser();
		CommandLine line = parser.parse(options, args);
		if (args.length < 1 || line.getOptionValue("data") == null) {
			printHelp(options);
		} else {
			String dataFile = line.getOptionValue("data");
			String idFile = line.getOptionValue("instanceId");
			String outdir = line.getOptionValue("outdir");
			int nFolds = Integer.parseInt(line.getOptionValue("folds", "10"));
			Integer nSeed = line.hasOption("seed") ? Integer.parseInt(line
					.getOptionValue("seed", "0")) : null;
			int nMinPerClass = Integer.parseInt(line.getOptionValue("minClass",
					"1"));
			Map<String, List<Integer>> mapLabelToLineNumbers = new HashMap<String, List<Integer>>();
			List<Integer> listIds = null;
			if (idFile != null)
				listIds = loadIdFile(idFile);
			List<String> lines = new ArrayList<String>();
			// read input, create a map of class label to line numbers, and a
			// list of each line
			readGramMatrix(dataFile, mapLabelToLineNumbers, lines);
			// split into folds
			List<Set<Integer>> folds = createFolds(mapLabelToLineNumbers,
					nFolds, nMinPerClass, nSeed);
			// output folds
			outputFolds(dataFile, folds, lines, listIds, outdir);
		}
	}

	private static List<Integer> loadIdFile(String idFile) throws IOException {
		List<Integer> listIds = new ArrayList<Integer>();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(idFile));
			String line = null;
			while ((line = r.readLine()) != null) {
				listIds.add(Integer.parseInt(line));
			}
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return listIds;
	}

	/**
	 * 
	 * @param gramFile
	 * @return 0 - directory file is in, 1 - file base name, 2 - file suffix
	 */
	private static String[] splitFile(String gramFile) {
		String base = "";
		String suffix = "";
		String dir = "";
		String prefix = gramFile;
		int nLastDot = gramFile.lastIndexOf(".");
		if (nLastDot > -1) {
			prefix = gramFile.substring(0, nLastDot);
			suffix = gramFile.substring(nLastDot);
		}
		int nLastDirSep = prefix.lastIndexOf(File.separator);
		nLastDirSep = Math.max(nLastDirSep, prefix.lastIndexOf("/"));
		if (nLastDirSep > -1) {
			dir = prefix.substring(0, nLastDirSep);
			base = prefix.substring(nLastDirSep + 1);
		} else {
			base = prefix;
		}
		return new String[] { dir, base, suffix };
	}

	/**
	 * write train/test cross-validation file pairs
	 * 
	 * @param gramFile
	 * @param folds
	 * @param lines
	 * @param mapLineNumberToId
	 * @throws IOException
	 */
	private static void outputFolds(String gramFile, List<Set<Integer>> folds,
			List<String> lines, List<Integer> listIds, String outdir)
			throws IOException {
		String fileParts[] = splitFile(gramFile);
		// if outdir specified, put the files there
		String foldOutDir = outdir != null ? outdir : fileParts[0];
		// add a / if needed
		if (foldOutDir.length() > 0 && !foldOutDir.endsWith(File.separator))
			foldOutDir += File.separator;
		String base = fileParts[1];
		String suffix = fileParts[2];
		String prefix = foldOutDir + base;
		for (int i = 0; i < folds.size(); i++) {
			String trainFileName = prefix + "_fold" + (i + 1) + "_train"
					+ suffix;
			String testFileName = prefix + "_fold" + (i + 1) + "_test" + suffix;
			String trainIdFileName = null;
			String testIdFileName = null;
			if (listIds != null) {
				trainIdFileName = prefix + "_fold" + (i + 1) + "_trainId"
						+ suffix;
				testIdFileName = prefix + "_fold" + (i + 1) + "_testId"
						+ suffix;
			}
			Set<Integer> setTrainLineNums = new TreeSet<Integer>();
			Set<Integer> setTestLineNums = new TreeSet<Integer>();
			int nFold = 0;
			for (Set<Integer> lineNums : folds) {
				if (nFold == i) {
					setTestLineNums.addAll(lineNums);
				} else {
					setTrainLineNums.addAll(lineNums);
				}
				nFold++;
			}
			writeSubSet(trainFileName, setTrainLineNums, lines,
					trainIdFileName, listIds);
			writeSubSet(testFileName, setTestLineNums, lines, testIdFileName,
					listIds);
		}
	}

	private static void writeSubSet(String fileName, Set<Integer> setLineNums,
			List<String> lines, String idFileName, List<Integer> listIds)
			throws IOException {
		BufferedWriter w = null;
		BufferedWriter wId = null;
		try {
			w = new BufferedWriter(new FileWriter(fileName));
			if (idFileName != null)
				wId = new BufferedWriter(new FileWriter(idFileName));
			for (int lineNum : setLineNums) {
				w.write(lines.get(lineNum));
				w.newLine();
				if (wId != null) {
					wId.write(Integer.toString(listIds.get(lineNum)));
					wId.newLine();
				}
			}
		} finally {
			try {
				if (w != null)
					w.close();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
			try {
				if (wId != null)
					wId.close();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * 
	 * @param mapLabelToLineNumbers
	 * @param nFolds
	 * @param nMinPerClass
	 * @param nSeed
	 * @return list with nFolds sets of line numbers corresponding to the folds
	 */
	public static List<Set<Integer>> createFolds(
			Map<String, List<Integer>> mapLabelToLineNumbers, int nFolds,
			int nMinPerClass, Integer nSeed) {
		Random r = new Random(nSeed != null ? nSeed : System
				.currentTimeMillis());
		List<Set<Integer>> folds = new ArrayList<Set<Integer>>(nFolds);
		Map<String, List<Set<Integer>>> mapLabelFolds = new HashMap<String, List<Set<Integer>>>();
		for (Map.Entry<String, List<Integer>> labelToLineNumber : mapLabelToLineNumbers
				.entrySet()) {
			List<Integer> lineNums = labelToLineNumber.getValue();
			Collections.shuffle(lineNums, r);
			List<Set<Integer>> labelFolds = new ArrayList<Set<Integer>>(nFolds);
			int blockSize = lineNums.size() / nFolds;
			for (int i = 0; i < nFolds; i++) {
				Set<Integer> foldLineNums = new HashSet<Integer>(blockSize);
				if (lineNums.size() <= nMinPerClass) {
					// we don't have minPerClass for the given class
					// just add all of them to each fold
					foldLineNums.addAll(lineNums);
				} else if (blockSize < nMinPerClass) {
					// too few of the given class - just randomly select
					// nMinPerClass
					double fraction = (double) nMinPerClass
							/ (double) lineNums.size();
					// iterate through the list, start somewhere in the middle
					int lineNumIndex = (int) (r.nextDouble() * lineNums.size());
					while (foldLineNums.size() < nMinPerClass) {
						// go back to beginning of list if we hit the end
						if (lineNumIndex >= lineNums.size()) {
							lineNumIndex = 0;
						}
						// randomly select this line
						if (r.nextDouble() <= fraction) {
							int nLineNum = lineNums.get(lineNumIndex);
							foldLineNums.add(nLineNum);
						}
						// go to next line
						lineNumIndex++;
					}
				} else {
					int nStart = i * blockSize;
					int nEnd = (i == nFolds - 1) ? lineNums.size() - 1 : nStart
							+ blockSize;
					for (int lineNumIndex = nStart; lineNumIndex < nEnd; lineNumIndex++) {
						foldLineNums.add(lineNums.get(lineNumIndex));
					}
				}
				labelFolds.add(foldLineNums);
			}
			mapLabelFolds.put(labelToLineNumber.getKey(), labelFolds);
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

	private static void readGramMatrix(String gramFile,
			Map<String, List<Integer>> mapLabelToLineNumbers, List<String> lines)
			throws FileNotFoundException, IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(gramFile));
			String line = null;
			int nLineNumber = 0;
			while ((line = r.readLine()) != null) {
				lines.add(line);
				String instanceLabel = CalculateMetrics.extractFirstToken(line,
						CalculateMetrics.wsPattern);
				addLineToMap(mapLabelToLineNumbers, instanceLabel, nLineNumber);
				nLineNumber++;
			}
			r.close();
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
					System.err.println("closing output");
					e.printStackTrace(System.err);
				}
			}
		}
	}

	private static void addLineToMap(
			Map<String, List<Integer>> mapLabelToLineNumbers,
			String instanceLabel, int nLineNumber) {
		List<Integer> lineNumbers = mapLabelToLineNumbers.get(instanceLabel);
		if (lineNumbers == null) {
			lineNumbers = new ArrayList<Integer>();
			mapLabelToLineNumbers.put(instanceLabel, lineNumbers);
		}
		lineNumbers.add(nLineNumber);
	}

}
