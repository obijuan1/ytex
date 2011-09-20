package ytex.kernel;

public interface InfoGainEvaluator {

	/**
	 * 
	 * @param labelQuery
	 *            query to get class counts per label - used to compute p(Y).
	 *            Returns following fields:
	 *            <ul>
	 *            <li>label (string)
	 *            <li>fold id (int)
	 *            <li>class (string)
	 *            <li>count (int)
	 *            </ul>
	 *            This must be sorted by label, fold id
	 * @param featureQuery
	 *            query to get feature frequency - used to compute p(X).
	 *            Optional; if not supplied will use feature frequency from
	 *            classFeatureFrequency. Returns following fields:
	 *            <ul>
	 *            <li>feature name (string)
	 *            <li>bin (string)
	 *            <li>frequency (double)
	 *            </ul>
	 *            Must be sorted by feature name
	 * @param classFeatureQuery
	 *            query to get joint class-feature counts for all folds for the
	 *            specfied label. Used to compute p(X,Y). Must contain named
	 *            parameter <code>label</code>. Must be ordered by fold and
	 *            feature name. Must return following fields:
	 *            <ul>
	 *            <li>fold id (int)
	 *            <li>feature name (string)
	 *            <li>class (string)
	 *            <li>feature bin (string)
	 *            <li>bin count (int)
	 *            </ul>
	 *            Iterate through the results and create the joint probability
	 *            table, and compute mutual information from this.
	 */
	public abstract void storeInfoGain(String name, String labelQuery,
			String featureQuery, String classFeatureQuery, Double minInfo);

}