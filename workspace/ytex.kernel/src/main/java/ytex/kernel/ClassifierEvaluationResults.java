package ytex.kernel;

import java.util.List;

public class ClassifierEvaluationResults {
	public List<Integer> getClassIds() {
		return classIds;
	}

	public void setClassIds(List<Integer> labels) {
		this.classIds = labels;
	}

	public List<ClassifierEvaluationResult> getResults() {
		return results;
	}

	public void setResults(List<ClassifierEvaluationResult> results) {
		this.results = results;
	}

	public int[] getPredictedClassIds() {
		int predictedClassLabels[] = new int[getResults().size()];
		int i = 0;
		for (ClassifierEvaluationResult result : getResults()) {
			predictedClassLabels[i] = result.getPredictedClassId();
			i++;
		}
		return predictedClassLabels;
	}

	public int[] getTargetClassIds() {
		int targetClassLabels[] = new int[getResults().size()];
		int i = 0;
		for (ClassifierEvaluationResult result : getResults()) {
			targetClassLabels[i] = result.getTargetClassId();
			i++;
		}
		return targetClassLabels;
	}

	public double[] getProbabilities() {
		double probabilities[] = new double[getResults().size()];
		int i = 0;
		for (ClassifierEvaluationResult result : getResults()) {
			probabilities[i] = result.getProbabilities()[0];
			i++;
		}
		return probabilities;
	}

	private List<Integer> classIds;
	private List<ClassifierEvaluationResult> results;
}
