package ytex.weka;

public interface WekaAttributeEvaluator {

	/**
	 * evaluate attributes in an arff file, save rank in db
	 * 
	 * @param name
	 *            corresponds to feature_eval.name
	 * @param corpusName
	 *            cv_fold.name
	 * @param arffFile
	 * @throws Exception
	 */
	public abstract void evaluateAttributesFromFile(String name,
			String corpusName, String arffFile) throws Exception;

	/**
	 * create instances from properties file, evaluate, save in db
	 * 
	 * @param name
	 *            feature set name. corresponds to feature_eval.name
	 * @param corpusName
	 *            cv_fold.name
	 * @param propFile
	 *            for SparseDataExporter
	 * @throws Exception
	 */
	public abstract void evaluateAttributesFromProps(String name,
			String corpusName, String propFile) throws Exception;

}