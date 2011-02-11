package ytex.weka;

import java.util.List;

/**
 * Import the weka results for the specified instance.
 */
public interface WekaResultInstanceImporter {
	/**
	 * 
	 * @param instanceNumber
	 *            optional instance number when multiple instance classified,
	 *            used for outputting errors/warnings
	 * @param instanceKey
	 *            list of attributes to resolve instance - foreign key to
	 *            document/sentence/whatever
	 * @param task
	 *            classification task
	 * @param classAuto
	 *            classifer's predicted class
	 * @param classGold
	 *            gold standard class index
	 * @param prediction
	 *            probabilities of belonging to specified classes, if run with
	 *            -distribution option. else just probability of belonging to
	 *            predicted class.
	 */
	public void importInstanceResult(Integer instanceNumber,
			List<String> instanceKey, String task, int classAuto,
			int classGold, List<Double> predictions);
}
