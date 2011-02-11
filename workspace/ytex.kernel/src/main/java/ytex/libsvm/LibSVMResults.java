package ytex.libsvm;

import java.util.List;

public class LibSVMResults {
	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<LibSVMResult> getResults() {
		return results;
	}

	public void setResults(List<LibSVMResult> results) {
		this.results = results;
	}

	public int[] getPredictedClassLabels() {
		int predictedClassLabels[] = new int[getResults().size()];
		int i = 0;
		for (LibSVMResult result : getResults()) {
			predictedClassLabels[i] = result.getPredictedClassIndex();
			i++;
		}
		return predictedClassLabels;
	}

	public int[] getTargetClassLabels() {
		int targetClassLabels[] = new int[getResults().size()];
		int i = 0;
		for (LibSVMResult result : getResults()) {
			targetClassLabels[i] = result.getTargetClassIndex();
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

	private List<String> labels;
	private List<LibSVMResult> results;
}
