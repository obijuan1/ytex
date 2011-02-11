package ytex.kernel;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * implements SCut strategy to optimize threshold for a given objective.
 * Currently only supports binary classification and F1.
 * 
 * @author vijay
 */
public class SCut {
	/*
	 * avoid dependencies on commons logging so that we can use this with
	 * weka/libsvm without too many additional dependencies.
	 */
	private static final Logger logger = Logger.getLogger(SCut.class.getName());

	public enum TargetStatistic {
		KAPPA, F1
	};

	/**
	 * get optimal threshold using SCut strategy. Currently support only binary
	 * classification
	 * 
	 * @param classScores
	 *            scores of 1st class
	 * @param classLabels
	 *            class labels of each instance
	 * @param targetClass
	 *            for f1 score evaluation
	 * @param targetStat
	 *            currently just F1
	 * @param predictedClassLabels
	 *            will be filled with predicted class labels best on optimal
	 *            threshold - optional (can be null)
	 * @return double optimal threshold
	 */
	public double getScutThreshold(double[] classScores, int[] classLabels,
			int targetClass, TargetStatistic targetStat,
			int[] predictedClassLabels) {
		SortedSet<Double> setThresholds = new TreeSet<Double>();
		double bestStatistic = 0;
		double bestThreshold = 0;
		int tempPredictedClassLabels[] = null;
		if (predictedClassLabels != null) {
			tempPredictedClassLabels = new int[classLabels.length];
		}
		// collect thresholds into a sorted set
		for (double instanceScore : classScores) {
			// round to 3 digits
			double score = ((int) (instanceScore * 1000)) / 1000.0;
			setThresholds.add(score);
		}
		// evaluate each threshold to find the optimum
		for (double threshold : setThresholds) {
			// TODO: is this convex? should we implement delta to determine when
			// we've peaked?
			double statistic = 0;
			if (TargetStatistic.F1.equals(targetStat)) {
				statistic = evaluateF1(classScores, classLabels, threshold,
						targetClass, tempPredictedClassLabels);
			}
			if (statistic > bestStatistic) {
				bestThreshold = threshold;
				bestStatistic = statistic;
				if (predictedClassLabels != null) {
					copyInto(tempPredictedClassLabels, predictedClassLabels);
				}
			}
		}
		return bestThreshold;
	}

	private void copyInto(int[] from, int[] to) {
		for (int i = 0; i < from.length; i++) {
			to[i] = from[i];
		}
	}

	/**
	 * evaluate F1 for the given scores
	 * 
	 * @param classScores
	 *            probabilities
	 * @param classLabels
	 *            'true' labels
	 * @param score
	 *            score to use as threshold
	 * @param targetClass
	 *            target class for calculating F1
	 * @param predictedClassLabels
	 *            predicted class labels from applying score as threshold
	 * @return
	 */
	private double evaluateF1(double[] classScores, int[] classLabels,
			double score, int targetClass, int[] predictedClassLabels) {
		for (int i = 0; i < classScores.length; i++) {
			predictedClassLabels[i] = classScores[i] < score ? 1 : 0;
		}
		IRMetrics ir = MetricUtil.calculateIRMetrics(classLabels,
				predictedClassLabels, targetClass);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("score:" + score + " precision:" + ir.getPrecision()
					+ " recall:" + ir.getRecall() + " f1:" + ir.getF1());
		}
		return ir.getF1();
	}

	/**
	 * apply the scut threshold to the given classes. trivial, but consolidated
	 * the logic here
	 * 
	 * @param classScores
	 * @param threshold
	 * @return
	 */
	public int[] applyScutThreshold(double[] classScores, double threshold) {
		int predictedClassLabels[] = new int[classScores.length];
		for (int i = 0; i < classScores.length; i++) {
			int predictedClass = classScores[i] < threshold ? 1 : 0;
			predictedClassLabels[i] = predictedClass;
		}
		return predictedClassLabels;
	}
}
