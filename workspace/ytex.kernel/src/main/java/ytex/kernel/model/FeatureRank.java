package ytex.kernel.model;

import java.io.Serializable;
import java.util.Comparator;

public class FeatureRank implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String featureName;
	private double evaluation;
	private int rank;

	public FeatureRank() {
	}

	public FeatureRank(String featureName, double evaluation) {
		this.featureName = featureName;
		this.evaluation = evaluation;
	}

	public double getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(double evaluation) {
		this.evaluation = evaluation;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * Sort features by descending evaluation score if two features have the
	 * same score, order them by name
	 * 
	 * @author vijay
	 * 
	 */
	public static class FeatureRankDesc implements Comparator<FeatureRank> {

		@Override
		public int compare(FeatureRank o1, FeatureRank o2) {
			if (o1.getEvaluation() > o2.getEvaluation()) {
				return -1;
			} else if (o1.getEvaluation() == o2.getEvaluation()) {
				return o1.getFeatureName().compareTo(o2.getFeatureName());
			} else {
				return 1;
			}
		}
	}

	/**
	 * Sort features by ascending evaluation score
	 * 
	 * @author vijay
	 * 
	 */
	public static class FeatureRankAsc implements Comparator<FeatureRank> {

		@Override
		public int compare(FeatureRank o1, FeatureRank o2) {
			if (o1.getEvaluation() > o2.getEvaluation()) {
				return 1;
			} else if (o1.getEvaluation() == o2.getEvaluation()) {
				return o1.getFeatureName().compareTo(o2.getFeatureName());
			} else {
				return -1;
			}
		}
	}
}
