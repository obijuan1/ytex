package ytex.svmlight;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ytex.kernel.SVMResult;
import ytex.kernel.SVMResults;
import ytex.kernel.model.SVMClassifierEvaluation;
import ytex.libsvm.LibSVMParser;

public class SVMLightParser extends LibSVMParser {
	static final Pattern psv = Pattern.compile("Number of SV:\\s(\\d+)\\s.*");
	static final Pattern pvc = Pattern
			.compile("Estimated VCdim of classifier: VCdim<=([\\d\\.]+)");

	/**
	 * Parse svm-classify input (instance file) and predictions (prediction
	 * file). instance file has target class id and attributes for each
	 * instance. predict file has value less than or greater than 0 for each
	 * instance, corresponding to class ids -1 and +1.
	 * 
	 * @param predictionFile
	 * @param instanceFile
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public SVMResults parse(String predictionFile, String instanceFile)
			throws Exception, IOException {
		SVMResults results = new SVMResults();
		List<SVMResult> listResults = new ArrayList<SVMResult>();
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
			while (((instanceLine = instanceReader.readLine()) != null)
					&& ((predictionLine = predictionReader.readLine()) != null)) {
				nLine++;
				SVMResult result = new SVMResult();
				listResults.add(result);
				String classIdTarget = extractFirstToken(instanceLine,
						wsPattern);
				result.setTargetClassId(Integer.parseInt(classIdTarget));
				int classIdPredicted = 0;
				try {
					double dPredict = Double.parseDouble(predictionLine);
					if (dPredict > 0)
						classIdPredicted = 1;
					else
						classIdPredicted = -1;
				} catch (NumberFormatException nfe) {
					System.err.println("error parsing:" + predictionLine);
					nfe.printStackTrace(System.err);
				}
				result.setPredictedClassId(classIdPredicted);
			}
		} finally {
			if (instanceReader != null) {
				try {
					instanceReader.close();
				} catch (Exception e) {
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

	@Override
	public SVMClassifierEvaluation parseClassifierEvaluation(String name,
			String experiment, String label, String options,
			String predictionFile, String instanceFile, String modelFile,
			String instanceIdFile, String trainOutputFile,
			boolean storeProbabilities) throws Exception {
		SVMClassifierEvaluation eval = super.initClassifierEval(name,
				experiment, label, options, instanceIdFile);
		eval.setAlgorithm("svmlight");
		this.parseTrainOutput(eval, trainOutputFile);
		// svmlight options identical to libsvm, with the exception of weight
		parseOptions(eval, options);
		return storeSVMResults(predictionFile, instanceFile, false,
				instanceIdFile, eval);
	}

	/**
	 * <pre>
	 * Number of SV: 133 (including 0 at upper bound)
	 * L1 loss: loss=0.00000
	 * Norm of weight vector: |w|=2.09380
	 * Norm of longest example vector: |x|=16.91153
	 * Estimated VCdim of classifier: VCdim<=684.90185
	 * </pre>
	 * 
	 * @param eval
	 * @param trainOutputFile
	 * @throws IOException
	 */
	private void parseTrainOutput(SVMClassifierEvaluation eval,
			String trainOutputFile) throws IOException {
		if (trainOutputFile == null)
			return;
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(trainOutputFile));
			String line = null;
			while ((line = r.readLine()) != null) {
				Matcher m = psv.matcher(line);
				if (m.matches())
					eval.setSupportVectors(Integer.parseInt(m.group(1)));
				m = pvc.matcher(line);
				if (m.matches())
					eval.setVcdim(Double.parseDouble(m.group(1)));
			}
		} catch (FileNotFoundException fnfe) {
			// ignore
		} finally {
			if (r != null)
				r.close();
		}
	}
}
