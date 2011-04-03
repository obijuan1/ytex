package ytex.kernel.model;

import java.io.Serializable;

public class FeatureInfogain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int featureInfogainId;
	String name;
	String label;
	Integer crossValidationFoldId;
	String featureName;
	double infogain;
	int rank;

	public FeatureInfogain(String name, String label,
			Integer crossValidationFoldId, String featureName, double infogain, int rank) {
		super();
		this.name = name;
		this.label = label;
		this.crossValidationFoldId = crossValidationFoldId;
		this.featureName = featureName;
		this.infogain = infogain;
		this.rank = rank;
	}

	public FeatureInfogain() {
		super();
	}

	public int getFeatureInfogainId() {
		return featureInfogainId;
	}

	public void setFeatureInfogainId(int featureInfogainId) {
		this.featureInfogainId = featureInfogainId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCrossValidationFoldId() {
		return crossValidationFoldId;
	}

	public void setCrossValidationFoldId(Integer crossValidationFoldId) {
		this.crossValidationFoldId = crossValidationFoldId;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public double getInfogain() {
		return infogain;
	}

	public void setInfogain(double infogain) {
		this.infogain = infogain;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
