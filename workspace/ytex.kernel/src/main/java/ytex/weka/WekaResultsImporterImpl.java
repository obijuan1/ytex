package ytex.weka;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parse weka instance output when classifier run with -p option. load results
 * into db.
 */
public class WekaResultsImporterImpl implements WekaResultsImporter {
	// inst# actual predicted error prediction (instance_id,C0000726,C0000731)
	// private static final Pattern patHeader = Pattern
	// .compile("\\sinst#\\s+actual\\s+predicted\\s+error\\s+");
	private static final Pattern patHeader = Pattern
			.compile("\\sinst#\\W.*\\Wactual\\W.*\\Wpredicted\\W.*\\Werror\\W");
	// 1 1:0 1:0 0.988 (330478,101,0)
	// private static final Pattern patResult = Pattern
	// .compile("\\s+(\\d+)\\s+(\\d+)\\:\\d+\\s+(\\d+)\\:\\d+\\s+\\+{0,1}\\s+(\\d\\.\\d+)\\s+\\((.*)\\)");
	private static final Pattern patResult = Pattern
			.compile("\\s+(\\d+)\\s+(\\d+)\\:.*\\s+(\\d+)\\:.*\\s+\\+{0,1}\\s+(.*)\\s+\\((.*)\\)");

	/**
	 * this imports the classification results for a document
	 */
	private DocumentResultInstanceImporter docResultInstanceImporter;

	public DocumentResultInstanceImporter getDocResultInstanceImporter() {
		return docResultInstanceImporter;
	}

	public void setDocResultInstanceImporter(
			DocumentResultInstanceImporter docResultInstanceImporter) {
		this.docResultInstanceImporter = docResultInstanceImporter;
	}

	/**
	 * Delegate to importResults
	 * 
	 * @see ytex.weka.WekaResultsImporter#importDocumentResults(java.lang.String,
	 *      java.io.BufferedReader)
	 */
	public void importDocumentResults(String task, BufferedReader reader)
			throws IOException {
		this.importResults(docResultInstanceImporter, task, reader);
	}

	/**
	 * Parse results, pass them off to WekaResultInstanceImporter
	 * 
	 * @see ytex.weka.WekaResultsImporter#importResults(ytex.weka.
	 *      WekaResultInstanceImporter, java.lang.String,
	 *      java.io.BufferedReader)
	 */
	public void importResults(
			WekaResultInstanceImporter resultInstanceImporter, String task,
			BufferedReader reader) throws IOException {
		String line = null;
		boolean keepGoing = true;
		boolean inResults = false;
		while ((line = reader.readLine()) != null && keepGoing) {
			if (!inResults) {
				// not yet in the results section - see if we found the header
				inResults = patHeader.matcher(line).find();
			} else {
				// in results section - see if this line contains some results
				Matcher matcher = patResult.matcher(line);
				if (matcher.find()) {
					// matches - parse it, pass it to the instance importer
					int instanceNum = Integer.parseInt(matcher.group(1));
					int classGold = Integer.parseInt(matcher.group(2));
					int classAuto = Integer.parseInt(matcher.group(3));
					// split something like
					// *0.988 0.012
					// or
					// *0.988,0.012
					// or just
					// 0.988
					String[] arrPredictionStr = matcher.group(4).split(
							"\\*|,|\\s");
					List<Double> listPredictions = new ArrayList<Double>(
							arrPredictionStr.length);
					for (String predStr : arrPredictionStr) {
						if (predStr.length() > 0)
							listPredictions.add(new Double(predStr));
					}
					List<String> instanceKey = Arrays.asList(matcher.group(5)
							.split(","));
					resultInstanceImporter.importInstanceResult(instanceNum,
							instanceKey, task, classAuto, classGold,
							listPredictions);
				} else {
					// hit end of results - stop
					keepGoing = false;
				}
			}
		}
	}
}
