package ytex.kernel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassifierEvalUtil {
	private static final Log log = LogFactory.getLog(ClassifierEvalUtil.class);
	Properties props;

	public ClassifierEvalUtil(String propFile) throws IOException {
		if (propFile != null)
			props = FileUtil.loadProperties(propFile, true);
		else
			props = System.getProperties();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String propFile = null;
		if (args.length > 0)
			propFile = args[0];
		ClassifierEvalUtil ceUtil = new ClassifierEvalUtil(propFile);
		ceUtil.generateEvalFiles();
	}

	private void generateEvalFiles() throws IOException {
		String algo = System.getProperty("kernel.algo");
		if ("semil".equalsIgnoreCase(algo)) {
			generateSemilEvalParams();
		} else if ("svmlight".equalsIgnoreCase(algo)
				|| "libsvm".equalsIgnoreCase(algo)) {
			generateSvmEvalParams(algo.toLowerCase());
		}
	}

	private void generateSvmEvalParams(String svmType) throws IOException {
		File kernelDataDir = new File(props.getProperty("kernel.data", "."));
		File[] trainFiles = kernelDataDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("train_data.txt");
			}
		});
		if (trainFiles != null && trainFiles.length > 0) {
			// iterate over label files
			for (File trainFile : trainFiles) {
				writeSvmEvalFile(trainFile, kernelDataDir, svmType);
			}
		}
	}

	/**
	 * generate parameter grid for each training file
	 * 
	 * @param trainFile
	 * @param kernelDataDir
	 * @param svmType
	 * @throws IOException
	 */
	private void writeSvmEvalFile(File trainFile, File kernelDataDir,
			String svmType) throws IOException {
		// list to hold the svm command lines
		List<String> evalLines = new ArrayList<String>();
		// label-specific weight parameters from a property file
		List<String> weightParams = getWeightParams(trainFile, svmType);
		// kernels to test
		List<String> kernels = Arrays.asList(props.getProperty("kernel.types")
				.split(","));
		// cost params
		List<String> costs = Arrays.asList(addOptionPrefix(
				props.getProperty("cv.costs").split(","), "-c "));
		// other general params
		List<String> libsvmEval = Arrays.asList(props.getProperty(
				"cv." + svmType + ".train.line", "").split(","));
		// iterate through kernel types, generate parameter grids
		for (String kernel : kernels) {
			List<String> kernelOpts = Arrays.asList(new String[] { "-t "
					+ kernel });
			if ("0".equals(kernel) || "4".equals(kernel)) {
				// linear/custom kernel - just cost & weight param
				evalLines.addAll(parameterGrid(libsvmEval, kernelOpts, costs,
						weightParams));
			} else if ("1".equals(kernel)) {
				// polynomial kernel - cost & weight & degree param
				evalLines.addAll(parameterGrid(libsvmEval, kernelOpts, costs,
						weightParams, Arrays.asList(addOptionPrefix(props
								.getProperty("cv.poly.degrees").split(","),
								"-d "))));
			} else if ("2".equals(kernel) || "3".equals(kernel)) {
				// polynomial kernel - cost & weight & gamma param
				evalLines.addAll(parameterGrid(libsvmEval, kernelOpts, costs,
						weightParams, Arrays
								.asList(addOptionPrefix(
										props.getProperty("cv.rbf.gammas")
												.split(","), "-g "))));
			}
		}
		if (evalLines.size() > 0) {
			String evalFile = trainFile.getPath().substring(0,
					trainFile.getPath().length() - 3)
					+ "properties";
			Properties evalProps = new Properties();
			evalProps.put("kernel.evalLines", listToString(evalLines));
			writeProps(evalFile, evalProps);
		}
	}

	private List<String> getWeightParams(File trainFile, String svmType)
			throws IOException {
		if ("libsvm".equals(svmType)) {
			String label = FileUtil.parseLabelFromFileName(trainFile.getName());
			// default label to 0
			label = label != null && label.length() > 0 ? label : "0";
			Properties weightProps = null;
			if (props.getProperty("kernel.classweights") != null) {
				weightProps = FileUtil.loadProperties(
						props.getProperty("kernel.classweights"), false);
				if (weightProps != null) {
					String weights = weightProps.getProperty("class.weight."
							+ label);
					if (weights != null && weights.length() > 0) {
						return Arrays.asList(weights.split(","));
					}
				}
			}
		}
		return new ArrayList<String>(0);
	}

	private void generateSemilEvalParams() throws IOException {
		File kernelDataDir = new File(props.getProperty("kernel.data", "."));
		List<String> evalLines = generateSemilEvalLines();
		File[] labelFiles = kernelDataDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("label.txt");
			}
		});
		if (labelFiles != null && labelFiles.length > 0) {
			// iterate over label files
			for (File labelFile : labelFiles) {
				List<String> distFiles = getSemilDistFilesForLabel(labelFile,
						kernelDataDir);
				if (distFiles != null)
					writeSemilEvalFile(distFiles, evalLines, labelFile);
			}
		}
	}

	/**
	 * convert list of strings to comma-delimited string;
	 * 
	 * @param listStr
	 * @return
	 */
	private String listToString(List<String> listStr) {
		StringBuilder b = new StringBuilder();
		boolean bfirst = true;
		for (String str : listStr) {
			if (!bfirst)
				b.append(",");
			b.append(str);
			bfirst = false;
		}
		return b.toString();
	}

	/**
	 * write file for label
	 * 
	 * @param distFiles
	 * @param evalLines
	 * @param labelFile
	 * @throws IOException
	 */
	private void writeSemilEvalFile(List<String> distFiles,
			List<String> evalLines, File labelFile) throws IOException {
		String labelFileName = labelFile.getPath();
		String evalFileName = labelFileName.substring(0,
				labelFileName.length() - 3) + "properties";
		Properties props = new Properties();
		props.setProperty("kernel.distFiles", listToString(distFiles));
		props.setProperty("kernel.evalLines", listToString(evalLines));
		writeProps(evalFileName, props);
	}

	private void writeProps(String evalFileName, Properties evalProps)
			throws IOException {
		if ("no".equalsIgnoreCase(props.getProperty("kernel.overwriteEvalFile",
				"yes"))) {
			File evalFile = new File(evalFileName);
			if (evalFile.exists()) {
				log.warn("skipping because eval file exists: " + evalFileName);
				return;
			}
		}
		BufferedWriter w = null;
		try {

			w = new BufferedWriter(new FileWriter(evalFileName));
			evalProps.store(w, null);
		} finally {
			if (w != null)
				w.close();
		}
	}

	/**
	 * generate command lines for semil
	 * 
	 * @return
	 */
	private List<String> generateSemilEvalLines() {
		// cv.rbf.gammas
		String gammas = props.getProperty("cv.rbf.gammas");
		List<String> gammaOpts = null;
		if (gammas != null && gammas.length() > 0) {
			gammaOpts = Arrays
					.asList(addOptionPrefix(gammas.split(","), "-g "));
		}
		// cv.semil.methods
		List<String> methods = Arrays.asList(props.getProperty(
				"cv.semil.methods", "").split(","));
		// semil.line
		List<String> semil = Arrays.asList(props.getProperty("cv.semil.line",
				"").split(","));
		return parameterGrid(semil, gammaOpts, methods);
	}

	private String[] addOptionPrefix(String[] args, String prefix) {
		String[] options = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			options[i] = prefix + args[i];
		}
		return options;
	}

	/**
	 * recursively generate parameter grid
	 * 
	 * @param lines
	 *            current lines
	 * @param params
	 *            variable number of List<String> arguments
	 * @return
	 */
	private List<String> parameterGrid(List<String> lines, Object... params) {
		List<String> newLines = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		List<String> paramList = (List<String>) params[0];
		if (paramList != null && paramList.size() > 0) {
			// only iterate over the list if it is non-empty
			for (String line : lines) {
				for (String param : paramList) {
					newLines.add(line + " " + param);
				}
			}
		} else {
			// else newLines = lines
			newLines.addAll(lines);
		}
		if (params.length > 1) {
			return parameterGrid(newLines,
					Arrays.copyOfRange(params, 1, params.length));
		} else {
			return newLines;
		}
	}

	private List<String> getSemilDistFilesForLabel(File labelFile,
			File kernelDataDir) {
		String labelFileName = labelFile.getName();
		String label = FileUtil.parseLabelFromFileName(labelFileName);
		Integer run = FileUtil.parseRunFromFileName(labelFileName);
		Integer fold = FileUtil.parseFoldFromFileName(labelFileName);
		File[] distFiles = null;
		// check fold scope
		if (fold != null) {
			String filePrefix = FileUtil.getFoldFilePrefix(null, label, run,
					fold) + "_dist_";
			distFiles = kernelDataDir.listFiles(new FileUtil.PrefixFileFilter(
					filePrefix));
		}
		// no matches, check label scope
		if ((distFiles == null || distFiles.length == 0) && label != null) {
			String filePrefix = FileUtil.getFoldFilePrefix(null, label, null,
					null) + "_dist_";
			distFiles = kernelDataDir.listFiles(new FileUtil.PrefixFileFilter(
					filePrefix));
		}
		// no matches, check unscoped
		if (distFiles == null || distFiles.length == 0) {
			distFiles = kernelDataDir.listFiles(new FileUtil.PrefixFileFilter(
					"dist_"));
		}
		if (distFiles != null && distFiles.length > 0) {
			List<String> listDistFiles = new ArrayList<String>(distFiles.length);
			for (File distFile : distFiles) {
				listDistFiles.add(distFile.getName());
			}
			return listDistFiles;
		} else {
			log.warn("no dist files match label file: " + labelFile);
			return null;
		}
	}

}
