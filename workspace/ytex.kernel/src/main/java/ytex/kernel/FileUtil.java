package ytex.kernel;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * miscellaneous utility functions used for data import/export
 * 
 * @author vijay
 * 
 */
public class FileUtil {
	static Pattern pFold = Pattern.compile("fold(\\d+)_");
	static Pattern pRun = Pattern.compile("run(\\d+)_");
	static Pattern pLabel = Pattern.compile("label(\\w+)_");

	/**
	 * extract fold from file name produced by file util
	 * 
	 * @param filename
	 * @return null if not in file name
	 */
	public static Integer parseFoldFromFileName(String filename) {
		Matcher m = pFold.matcher(filename);
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		} else
			return null;
	}

	/**
	 * extract run from file name produced by file util
	 * 
	 * @param filename
	 * @return null if not in file name
	 */
	public static Integer parseRunFromFileName(String filename) {
		Matcher m = pRun.matcher(filename);
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		} else
			return null;
	}

	/**
	 * extract label from file name produced by file util
	 * 
	 * @param filename
	 * @return null if not in file name
	 */
	public static String parseLabelFromFileName(String filename) {
		Matcher m = pLabel.matcher(filename);
		if (m.find()) {
			return m.group(1);
		} else
			return null;
	}

	/**
	 * construct file name with label, run, fold with format
	 * <tt>label[label]_run[run]_fold[fold]_</tt> only put in the non-null
	 * pieces.
	 * 
	 * @param outdir
	 * @param label
	 * @param run
	 * @param fold
	 * @return
	 */
	public static String getFoldFilePrefix(String outdir, String label,
			Integer run, Integer fold) {
		StringBuilder builder = new StringBuilder();
		if (outdir != null && outdir.length() > 0) {
			builder.append(outdir);
			if (!outdir.endsWith("/") && !outdir.endsWith("\\"))
				builder.append(File.separator);
		}
		if (label != null && label.length() > 0) {
			builder.append("label").append(label);
			if ((run != null && run > 0) || (fold != null && fold > 0))
				builder.append(label).append("_");
		}
		if (run != null && run > 0) {
			builder.append("run").append(Integer.toString(run));
			if (fold != null && fold > 0)
				builder.append("_");
		}
		if (fold != null && fold > 0) {
			builder.append("fold").append(Integer.toString(fold));
		}
		return builder.toString();
	}

	/**
	 * construct file name for train/test set, will be like
	 * <tt>label[label]_run[run]_fold[fold]_train</tt>
	 * 
	 * @param outdir
	 * @param label
	 * @param run
	 * @param fold
	 * @param train
	 * @return
	 */
	public static String getDataFilePrefix(String outdir, String label,
			Integer run, Integer fold, Boolean train) {
		StringBuilder builder = new StringBuilder(getFoldFilePrefix(outdir,
				label, run, fold));
		if (train != null) {
			if ((label != null && label.length() > 0)
					|| (run != null && run > 0) || (fold != null && fold > 0))
				builder.append("_");
			if (train.booleanValue())
				builder.append("train");
			else
				builder.append("test");
		}
		return builder.toString();
	}

	public static void createOutdir(String outdir) throws IOException {
		if (outdir != null && outdir.length() > 0) {
			File outdirF = new File(outdir);
			if (outdirF.exists()) {
				if (!outdirF.isDirectory()) {
					throw new IOException(
							"outdir exists but is not a directory " + outdir);
				}
			} else {
				if (!outdirF.mkdirs()) {
					throw new IOException("could not create directory: "
							+ outdir);
				}
			}
		}

	}

}
