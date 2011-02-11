package ytex.libsvm;

public class LibSVMResult {
	int targetClassIndex;
	int predictedClassIndex;
	int instanceId;
	double[] probabilities;

	public int getTargetClassIndex() {
		return targetClassIndex;
	}

	public void setTargetClassIndex(int targetClassIndex) {
		this.targetClassIndex = targetClassIndex;
	}

	public int getPredictedClassIndex() {
		return predictedClassIndex;
	}

	public void setPredictedClassIndex(int predictedClassIndex) {
		this.predictedClassIndex = predictedClassIndex;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public double[] getProbabilities() {
		return probabilities;
	}

	public void setProbabilities(double[] probabilities) {
		this.probabilities = probabilities;
	}

}
