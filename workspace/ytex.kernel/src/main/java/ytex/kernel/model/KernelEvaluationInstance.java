package ytex.kernel.model;

import java.io.Serializable;

public class KernelEvaluationInstance implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int instanceId1;
	private int instanceId2;

	private KernelEvaluation kernelEvaluation;

	private double similarity;
	public KernelEvaluationInstance() {
		super();
	}
	public KernelEvaluationInstance(KernelEvaluation kernelEvaluation,
			int instanceId1, int instanceId2, double similarity) {
		super();
		this.kernelEvaluation = kernelEvaluation;
		this.instanceId1 = instanceId1;
		this.instanceId2 = instanceId2;
		this.similarity = similarity;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KernelEvaluationInstance other = (KernelEvaluationInstance) obj;
		if (instanceId1 != other.instanceId1)
			return false;
		if (instanceId2 != other.instanceId2)
			return false;
		if (!kernelEvaluation.equals(kernelEvaluation.getKernelEvaluationId()))
			return false;
		return true;
	}

	public int getInstanceId1() {
		return instanceId1;
	}

	public int getInstanceId2() {
		return instanceId2;
	}

	public KernelEvaluation getKernelEvaluation() {
		return kernelEvaluation;
	}

	public double getSimilarity() {
		return similarity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + instanceId1;
		result = prime * result + instanceId2;
		result = prime
				* result
				+ ((kernelEvaluation == null) ? 0 : kernelEvaluation.hashCode());
		return result;
	}

	public void setInstanceId1(int instanceId1) {
		this.instanceId1 = instanceId1;
	}

	public void setInstanceId2(int instanceId2) {
		this.instanceId2 = instanceId2;
	}

	public void setKernelEvaluation(KernelEvaluation kernelEvaluation) {
		this.kernelEvaluation = kernelEvaluation;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

}
