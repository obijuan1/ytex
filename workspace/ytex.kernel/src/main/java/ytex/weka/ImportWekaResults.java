package ytex.weka;

import java.io.BufferedReader;
import java.io.FileReader;

import ytex.kernel.KernelContextHolder;

/**
 * Runs ytex.weka.WekaResultsImporter for the specified task and result file.
 * 
 * @see WekaResultsImporter
 * @author vijay
 * 
 */
public class ImportWekaResults {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0 || args.length > 2) {
			System.out
					.println("Usage: java ytex.weka.ImportWekaResults <task name> <file name>");
		} else {
			WekaResultsImporter importer = (WekaResultsImporter) KernelContextHolder
					.getApplicationContext().getBean("wekaResultsImporter");
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(args[1]));
				importer.importDocumentResults(args[0], reader);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}
	}

}
