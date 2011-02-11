package ytex.libsvm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LibSVMParser {
	public static Pattern wsPattern = Pattern.compile("\\s");
	public static Pattern wsDotPattern = Pattern.compile("\\s|\\.|\\z");
	public static Pattern labelsPattern = Pattern.compile("labels\\s+(.*)");
	public static Pattern totalSVPattern = Pattern.compile("total_sv (\\d+)");

	/**
	 * parse svm-train model file to get the number of support vectors. Needed
	 * for model selection
	 * 
	 * @param modelFile
	 * @return
	 * @throws IOException
	 */
	public Integer parseModel(String modelFile) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(modelFile));
			String line = null;
			while ((line = r.readLine()) != null) {
				Matcher m = totalSVPattern.matcher(line);
				if (m.find()) {
					return new Integer(m.group(1));
				}
			}
		} finally {
			try {
				if (r != null)
					r.close();
			} catch (Exception e) {
				System.err.println("reading model file");
				e.printStackTrace(System.err);
			}
		}
		return null;
	}

	/**
	 * Parse svm-predict input (instance file) and predictions (prediction file)
	 * 
	 * @param predictionFile
	 * @param instanceFile
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public LibSVMResults parse(String predictionFile, String instanceFile)
			throws Exception, IOException {
		LibSVMResults results = new LibSVMResults();
		List<LibSVMResult> listResults = new ArrayList<LibSVMResult>();
		results.setResults(listResults);
		BufferedReader instanceReader = null;
		BufferedReader predictionReader = null;
		try {
			instanceReader = new BufferedReader(new FileReader(instanceFile));
			predictionReader = new BufferedReader(
					new FileReader(predictionFile));
			String instanceLine = null;
			String predictionLine = null;
			int nLine = 0;
			// 1st line in libSVMOutputReader lists labels
			List<String> labels = null;
			labels = parseLabels(predictionReader, labels);
			if (labels.size() < 2)
				throw new Exception("error parsing labels");
			while (((instanceLine = instanceReader.readLine()) != null)
					&& ((predictionLine = predictionReader.readLine()) != null)) {
				nLine++;
				LibSVMResult result = new LibSVMResult();
				listResults.add(result);
				String predictTokens[] = wsPattern.split(predictionLine);
				String labelPredict = predictTokens[0];
				String labelTruth = extractFirstToken(instanceLine, wsPattern);
				result.setTargetClassIndex(labels.indexOf(labelTruth));
				result.setPredictedClassIndex(labels.indexOf(labelPredict));
				if (predictTokens.length > 1) {
					double probabilities[] = new double[labels.size()];
					for (int i = 1; i < predictTokens.length; i++) {
						probabilities[i - 1] = Double
								.parseDouble(predictTokens[i]);
					}
					result.setProbabilities(probabilities);
				}
			}
		} finally {
			if (instanceReader != null) {
				try {
					instanceReader.close();
				} catch (Exception e) {
					System.err.println("testGramReader");
					e.printStackTrace(System.err);
				}
			}
			if (predictionReader != null) {
				try {
					predictionReader.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return results;
	}

	public static String extractFirstToken(String line, Pattern tokDelimPattern) {
		Matcher wsMatcher = tokDelimPattern.matcher(line);
		String token = null;
		if (wsMatcher.find() && wsMatcher.start() > 0) {
			token = line.substring(0, wsMatcher.start());
		}
		return token;
	}

	private List<String> parseLabels(BufferedReader predictionReader,
			List<String> labels) throws IOException {
		String labelLine = predictionReader.readLine();
		Matcher labelMatcher = labelsPattern.matcher(labelLine);
		if (labelMatcher.find()) {
			String labelsA[] = wsPattern.split(labelMatcher.group(1));
			if (labelsA != null && labelsA.length > 0) {
				labels = new ArrayList<String>(labelsA.length);
				Collections.addAll(labels, labelsA);
			}
		}
		return labels;
	}

	public static void main(String args[]) throws Exception {
		LibSVMParser parser = new LibSVMParser();
		parser
				.parse(
						"E:\\projects\\ytex\\sujeevan\\processedData\\predict_35.txt",
						"E:\\projects\\ytex\\sujeevan\\processedData\\test_gram_35.txt");

	}
}
