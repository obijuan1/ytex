package ytex.kernel.model;

import java.io.Serializable;
import java.util.List;

public class FeatureEvaluation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int featureEvaluationId;
	private String name;
	private String label;
	private Integer crossValidationFoldId;
	private String evaluationType;
	private List<FeatureRank> features;

	public FeatureEvaluation() {
		super();
	}

	public FeatureEvaluation(String name, String label,
			Integer crossValidationFoldId, String evaluationType,
			List<FeatureRank> features) {
		super();
		this.name = name;
		this.label = label;
		this.crossValidationFoldId = crossValidationFoldId;
		this.evaluationType = evaluationType;
		this.features = features;
	}

	public int getFeatureEvaluationId() {
		return featureEvaluationId;
	}

	public void setFeatureEvaluationId(int featureEvaluationId) {
		this.featureEvaluationId = featureEvaluationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getCrossValidationFoldId() {
		return crossValidationFoldId;
	}

	public void setCrossValidationFoldId(Integer crossValidationFoldId) {
		this.crossValidationFoldId = crossValidationFoldId;
	}

	public String getEvaluationType() {
		return evaluationType;
	}

	public void setEvaluationType(String evaluationType) {
		this.evaluationType = evaluationType;
	}

	public List<FeatureRank> getFeatures() {
		return features;
	}

	public void setFeatures(List<FeatureRank> features) {
		this.features = features;
	}

}
