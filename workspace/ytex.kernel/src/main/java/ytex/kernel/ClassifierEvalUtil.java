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
		}
	}

	private void generateSemilEvalParams() throws IOException {
		File kernelDataDir = new File(System.getProperty("kernel.data", "."));
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
		BufferedWriter w = null;
		try {
			String labelFileName = labelFile.getPath();
			String evalFileName = labelFileName.substring(0,
					labelFileName.length() - 3)
					+ "properties";
			w = new BufferedWriter(new FileWriter(evalFileName));
			Properties props = new Properties();
			props.setProperty("kernel.distFiles", listToString(distFiles));
			props.setProperty("kernel.evalLines", listToString(evalLines));
			props.store(w, null);
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
		List<String> semil = Arrays.asList(props.getProperty("semil.line", "")
				.split(","));
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
	 *            variable number of Iterable<String> arguments
	 * @return
	 */
	private List<String> parameterGrid(Iterable<String> lines, Object... params) {
		List<String> newLines = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Iterable<String> paramList = (Iterable<String>) params[0];
		for (String line : lines) {
			for (String param : paramList) {
				newLines.add(line + " " + param);
			}
		}
		if (params.length > 1) {
			return parameterGrid(newLines,
					Arrays.copyOfRange(params, 1, params.length));
		} else {
			return newLines;
		}
	}

	private List<String> getSemilDistFilesForLabel(File labelFile, File kernelDataDir) {
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
					"_dist_"));
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
