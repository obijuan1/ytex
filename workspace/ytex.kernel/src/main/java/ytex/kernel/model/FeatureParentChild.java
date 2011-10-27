package ytex.kernel.model;

import java.io.Serializable;

public class FeatureParentChild implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	FeatureRank featureRankChild;
	FeatureRank featureRankParent;
	int featureParentChildId;
	public FeatureRank getFeatureRankChild() {
		return featureRankChild;
	}
	public void setFeatureRankChild(FeatureRank featureRankChild) {
		this.featureRankChild = featureRankChild;
	}
	public FeatureRank getFeatureRankParent() {
		return featureRankParent;
	}
	public void setFeatureRankParent(FeatureRank featureRankParent) {
		this.featureRankParent = featureRankParent;
	}
	public int getFeatureParentChildId() {
		return featureParentChildId;
	}
	public void setFeatureParentChildId(int featureParentChildId) {
		this.featureParentChildId = featureParentChildId;
	}
}
