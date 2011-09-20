package ytex.weka;

import java.util.Properties;

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
	public abstract void evaluateAttributesFromFile(String corpusName,
			String featureSetName, String splitName, String arffFile)
			throws Exception;

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
	public abstract void evaluateAttributesFromProps(String corpusName,
			String splitName, String featureSetName, Properties props)
			throws Exception;

}