package ytex.kernel.model;

import java.io.Serializable;

public class FeatureInfogain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int fatureInfogainId;
	String name;
	String label;
	Integer cvFoldId;
	String featureName;
	double infogain;
	int rank;

	public FeatureInfogain(int fatureInfogainId, String name, String label,
			Integer cvFoldId, String featureName, double infogain, int rank) {
		super();
		this.fatureInfogainId = fatureInfogainId;
		this.name = name;
		this.label = label;
		this.cvFoldId = cvFoldId;
		this.featureName = featureName;
		this.infogain = infogain;
		this.rank = rank;
	}

	public FeatureInfogain() {
		super();
	}

	public int getFatureInfogainId() {
		return fatureInfogainId;
	}

	public void setFatureInfogainId(int fatureInfogainId) {
		this.fatureInfogainId = fatureInfogainId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCvFoldId() {
		return cvFoldId;
	}

	public void setCvFoldId(Integer cvFoldId) {
		this.cvFoldId = cvFoldId;
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

}
