package ytex.kernel.model;

import java.io.Serializable;
import java.util.List;

import ytex.dao.DBUtil;

public class FeatureEvaluation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String corpusName;
	private int crossValidationFoldId = 0;
	private String evaluationType;
	private int featureEvaluationId;
	private List<FeatureRank> features;
	private String featureSetName = DBUtil.getEmptyString();
	private String label = DBUtil.getEmptyString();
	private String param1 = DBUtil.getEmptyString();

	public FeatureEvaluation() {
		super();
	}

	public FeatureEvaluation(String name, String label,
			Integer crossValidationFoldId, String evaluationType,
			List<FeatureRank> features) {
		super();
		this.corpusName = name;
		this.label = label;
		this.crossValidationFoldId = crossValidationFoldId;
		this.evaluationType = evaluationType;
		this.features = features;
	}

	public String getCorpusName() {
		return corpusName;
	}

	public int getCrossValidationFoldId() {
		return crossValidationFoldId;
	}

	public String getEvaluationType() {
		return evaluationType;
	}

	public int getFeatureEvaluationId() {
		return featureEvaluationId;
	}

	public List<FeatureRank> getFeatures() {
		return features;
	}

	public String getFeatureSetName() {
		return featureSetName;
	}

	public String getLabel() {
		return label;
	}

	public String getParam1() {
		return param1;
	}

	public void setCorpusName(String name) {
		this.corpusName = name;
	}

	public void setCrossValidationFoldId(int crossValidationFoldId) {
		this.crossValidationFoldId = crossValidationFoldId;
	}

	public void setEvaluationType(String evaluationType) {
		this.evaluationType = evaluationType;
	}

	public void setFeatureEvaluationId(int featureEvaluationId) {
		this.featureEvaluationId = featureEvaluationId;
	}

	public void setFeatures(List<FeatureRank> features) {
		this.features = features;
	}

	public void setFeatureSetName(String featureSetName) {
		this.featureSetName = DBUtil.nullToEmptyString(featureSetName);
	}

	public void setLabel(String label) {
		this.label = DBUtil.nullToEmptyString(label);
	}

	public void setParam1(String param1) {
		this.param1 = DBUtil.nullToEmptyString(param1);
	}

}
