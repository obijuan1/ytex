package ytex.libsvm;

import java.util.List;

public class LibSVMResults {
	public List<Integer> getClassIds() {
		return classIds;
	}

	public void setClassIds(List<Integer> labels) {
		this.classIds = labels;
	}

	public List<LibSVMResult> getResults() {
		return results;
	}

	public void setResults(List<LibSVMResult> results) {
		this.results = results;
	}

	public int[] getPredictedClassIds() {
		int predictedClassLabels[] = new int[getResults().size()];
		int i = 0;
		for (LibSVMResult result : getResults()) {
			predictedClassLabels[i] = result.getPredictedClassId();
			i++;
		}
		return predictedClassLabels;
	}

	public int[] getTargetClassIds() {
		int targetClassLabels[] = new int[getResults().size()];
		int i = 0;
		for (LibSVMResult result : getResults()) {
			targetClassLabels[i] = result.getTargetClassId();
			i++;
		}
		return targetClassLabels;
	}

	public double[] getProbabilities() {
		double probabilities[] = new double[getResults().size()];
		int i = 0;
		for (LibSVMResult result : getResults()) {
			probabilities[i] = result.getProbabilities()[0];
			i++;
		}
		return probabilities;
	}

	private List<Integer> classIds;
	private List<LibSVMResult> results;
}
