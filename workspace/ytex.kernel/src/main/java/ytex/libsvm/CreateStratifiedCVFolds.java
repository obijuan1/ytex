package ytex.libsvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public class CreateStratifiedCVFolds {
	// private static final Log log = LogFactory
	// .getLog(CreateStratifiedCVFolds.class);

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out
					.println("usage: java ytex.libsvm.CreateStratifiedCVFolds <gram matrix> [folds] [random seed] [min instances per class]");
		} else {
			String gramFile = args[0];
			int nFolds = Integer.parseInt(args[1]);
			int nSeed = 0;
			int nMinPerClass = 2;
			if (args.length > 2) {
				nSeed = Integer.parseInt(args[2]);
			}
			if (args.length > 3) {
				nMinPerClass = Integer.parseInt(args[3]);
			}
			Map<String, List<Integer>> mapLabelToLineNumbers = new HashMap<String, List<Integer>>();
			List<String> lines = new ArrayList<String>();
			// read input, create a map of class label to line numbers, and a
			// list
			// of each line
			readGramMatrix(gramFile, mapLabelToLineNumbers, lines);
			// split into folds
			List<Set<Integer>> folds = createFolds(mapLabelToLineNumbers,
					nFolds, nMinPerClass, nSeed);
			// output folds
			outputFolds(gramFile, folds, lines);
		}
	}

	/**
	 * write train/test cross-validation file pairs
	 * 
	 * @param gramFile
	 * @param folds
	 * @param lines
	 * @throws IOException
	 */
	private static void outputFolds(String gramFile, List<Set<Integer>> folds,
			List<String> lines) throws IOException {
		int nLastDot = gramFile.lastIndexOf(".");
		String prefix = gramFile;
		String suffix = "";
		if (nLastDot >= 0) {
			prefix = gramFile.substring(0, nLastDot);
			suffix = gramFile.substring(nLastDot);
		}
		for (int i = 0; i < folds.size(); i++) {
			String trainFileName = prefix + "_fold" + (i + 1) + "_train"
					+ suffix;
			String testFileName = prefix + "_fold" + (i + 1) + "_test" + suffix;
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
			writeSubSet(trainFileName, setTrainLineNums, lines);
			writeSubSet(testFileName, setTestLineNums, lines);
		}
	}

	private static void writeSubSet(String fileName, Set<Integer> setLineNums,
			List<String> lines) throws IOException {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(fileName));
			for (int lineNum : setLineNums) {
				w.write(lines.get(lineNum));
				w.newLine();
			}
		} finally {
			try {
				w.close();
			} catch (Exception e) {
				System.err.println(e.getMessage());
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
	 * @return
	 */
	private static List<Set<Integer>> createFolds(
			Map<String, List<Integer>> mapLabelToLineNumbers, int nFolds,
			int nMinPerClass, int nSeed) {
		Random r = new Random(System.currentTimeMillis());
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
