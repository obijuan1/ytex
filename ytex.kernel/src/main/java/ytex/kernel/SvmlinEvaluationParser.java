package ytex.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.model.SVMClassifierEvaluation;

public class SvmlinEvaluationParser extends BaseClassifierEvaluationParser {
	private static final Log log = LogFactory.getLog(SvmlinEvaluationParser.class);
	public static Pattern pAlgo = Pattern.compile("-A\\s+(\\d)");
	public static Pattern pLambdaW = Pattern.compile("-W\\s+([\\d\\.eE-]+)");
	public static Pattern pLambaU = Pattern.compile("-U\\s+([\\d\\.eE-]+)");

	/**
	 * parse directory. Expect following files:
	 * <ul>
	 * <li>model.txt - libsvm model file
	 * <li>options.properties - properties file with needed parameter settings
	 * (see ParseOption)
	 * <li>predict.txt - predictions on test set
	 * </ul>
	 */
	@Override
	public void parseDirectory(File dataDir, File outputDir) throws IOException {
		String model = outputDir.getPath() + File.separator + "weights";
		String predict = outputDir.getPath() + File.separator + "outputs";
		String optionsFile = outputDir.getPath() + File.separator
				+ "options.properties";
		if (checkFileRead(model) && checkFileRead(predict)
				&& checkFileRead(optionsFile)) {
			// read options.properties
			Properties props = this.loadProps(outputDir);
			SVMClassifierEvaluation eval = new SVMClassifierEvaluation();
			// set algorithm
			eval.setAlgorithm("svmlin");
			// parse results
			parseResults(dataDir, outputDir, model, predict, eval, props);
			// save the classifier evaluation
			storeSemiSupervised(props, eval);			
		}
	}

	private void parseResults(File dataDir, File outputDir, String model,
			String predict, SVMClassifierEvaluation eval, Properties props) throws IOException {
		// parse fold, run, label from file base name
		String fileBaseName = this.getFileBaseName(props);
		initClassifierEvaluation(fileBaseName, eval);
		// initialize common properties
		initClassifierEvaluationFromProperties(props, eval);
		// parse options from command line
		String options = props.getProperty(ParseOption.EVAL_LINE.getOptionKey());
		if (options != null) {
			eval.setKernel(parseIntOption(pAlgo, options));
			if (eval.getKernel() == null)
				eval.setKernel(1);
			eval.setCost(parseDoubleOption(pLambdaW, options));
			eval.setGamma(parseDoubleOption(pLambaU, options));
		}		
		// parse predictions
		if (fileBaseName != null && fileBaseName.length() > 0) {
			List<List<Long>> listClassInfo = loadClassInfo(dataDir, fileBaseName);
			// process .output files
			if (listClassInfo != null) {
				parseSvmlinOutput(eval, fileBaseName, props, predict, listClassInfo);
			}
		} else {
			log.warn("couldn't parse directory; kernel.label.base not defined. Dir: "
					+ outputDir);
		}

	}

	private void parseSvmlinOutput(SVMClassifierEvaluation eval, String fileBaseName, Properties props,
			String predict, List<List<Long>> listClassInfo) throws IOException {
		BufferedReader outputReader = null;
		boolean storeUnlabeled = YES.equalsIgnoreCase(props
				.getProperty(
						ParseOption.STORE_UNLABELED.getOptionKey(),
						ParseOption.STORE_UNLABELED.getDefaultValue()));
		try {
			int classIds[] = new int[listClassInfo.size()];
			int i = 0;
			outputReader = new BufferedReader(new FileReader(predict));
			String classId = null;
			while ((classId = outputReader.readLine()) != null) {
				if(i < listClassInfo.size())
					classIds[i++]= (Double.parseDouble(classId) > 0 ? 1 : -1);
				else
					throw new IOException(predict+":  more predictions than expected");
			}
			if(i < listClassInfo.size()-1)
				throw new IOException(predict+":  less predictions than expected");
			super.updateSemiSupervisedPredictions(eval, listClassInfo,
					storeUnlabeled, classIds);
		} finally {
			if (outputReader != null) {
				try {
					outputReader.close();
				} catch (Exception ignore) {
				}
			}
		}
	}
}
