package ytex.libsvm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ytex.kernel.model.ClassifierInstanceEvaluation;
import ytex.kernel.model.LibSVMClassifierEvaluation;

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

			results.setClassIds(parseClassIds(predictionReader));
			if (results.getClassIds().size() < 2)
				throw new Exception("error parsing class ids");
			while (((instanceLine = instanceReader.readLine()) != null)
					&& ((predictionLine = predictionReader.readLine()) != null)) {
				nLine++;
				LibSVMResult result = new LibSVMResult();
				listResults.add(result);
				String predictTokens[] = wsPattern.split(predictionLine);
				String classIdPredicted = predictTokens[0];
				String classIdTarget = extractFirstToken(instanceLine,
						wsPattern);
				result.setTargetClassId(Integer.parseInt(classIdTarget));
				result.setPredictedClassId(Integer.parseInt(classIdPredicted));
				if (predictTokens.length > 1) {
					double probabilities[] = new double[results.getClassIds()
							.size()];
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

	private List<Integer> parseClassIds(BufferedReader predictionReader)
			throws IOException {
		List<Integer> labels = null;
		String labelLine = predictionReader.readLine();
		Matcher labelMatcher = labelsPattern.matcher(labelLine);
		if (labelMatcher.find()) {
			String labelsA[] = wsPattern.split(labelMatcher.group(1));
			if (labelsA != null && labelsA.length > 0) {
				labels = new ArrayList<Integer>(labelsA.length);
				for (String label : labelsA)
					labels.add(Integer.parseInt(label));
			}
		}
		return labels;
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

	public LibSVMClassifierEvaluation parseClassifierEvaluation(String name,
			String experiment, String label, String options, String fold,
			String predictionFile, String instanceFile, String modelFile,
			String instanceIdFile) throws Exception {
		List<Integer> instanceIds = null;
		if (instanceIdFile != null)
			instanceIds = parseInstanceIds(instanceIdFile);
		LibSVMClassifierEvaluation eval = new LibSVMClassifierEvaluation();
		eval.setFold(fold);
		eval.setName(name);
		eval.setExperiment(experiment);
		eval.setAlgorithm("libsvm");
		eval.setOptions(options);
		eval.setLabel(label);
		eval.setSupportVectors(this.parseModel(modelFile));
		parseOptions(eval, options);
		LibSVMResults results = this.parse(predictionFile, instanceFile);
		int j = 0;
		for (LibSVMResult result : results.getResults()) {
			int instanceId = j++;
			if (instanceIds != null)
				instanceId = instanceIds.get(instanceId);
			ClassifierInstanceEvaluation instanceEval = new ClassifierInstanceEvaluation();
			instanceEval.setPredictedClassId(result.getPredictedClassId());
			instanceEval.setTargetClassId(result.getTargetClassId());
			instanceEval.setClassifierEvaluation(eval);
			instanceEval.setInstanceId(instanceId);
			for (int i = 0; i < result.getProbabilities().length; i++) {
				instanceEval.getClassifierInstanceProbabilities().put(
						results.getClassIds().get(i),
						result.getProbabilities()[i]);
			}
			eval.getClassifierInstanceEvaluations().put(instanceId,
					instanceEval);
		}
		return eval;
	}

	private void parseOptions(LibSVMClassifierEvaluation eval, String options) {
		// -q -b 1 -t 2 -w1 41 -g 1000 -c 1000 training_data_11_fold9_train.txt
		// training_data_11_fold9_model.txt
		if (options != null) {
			Pattern pKernel = Pattern.compile("-t\\s+(\\d)");
			Pattern pGamma = Pattern.compile("-g\\s+([\\d\\.-e]+)");
			Pattern pCost = Pattern.compile("-c\\s+([\\d\\.-e]+)");
			Pattern pWeight = Pattern.compile("-w1\\s+(\\d+)");
			Pattern pDegree = Pattern.compile("-d\\s+(\\d+)");
			eval.setKernel(parseIntOption(pKernel, options));
			eval.setDegree(parseIntOption(pDegree, options));
			eval.setWeight(parseIntOption(pWeight, options));
			eval.setCost(parseDoubleOption(pCost, options));
			eval.setGamma(parseDoubleOption(pGamma, options));
		}
	}

	private Double parseDoubleOption(Pattern pCost, String options) {
		Matcher m = pCost.matcher(options);
		if (m.find())
			return Double.parseDouble(m.group(1));
		else
			return null;
	}

	private Integer parseIntOption(Pattern pKernel, String options) {
		Matcher m = pKernel.matcher(options);
		if (m.find())
			return Integer.parseInt(m.group(1));
		else
			return null;
	}

	public static void main(String args[]) throws Exception {
		LibSVMParser parser = new LibSVMParser();
		parser
				.parse(
						"E:\\projects\\ytex\\sujeevan\\processedData\\predict_35.txt",
						"E:\\projects\\ytex\\sujeevan\\processedData\\test_gram_35.txt");

	}
}
