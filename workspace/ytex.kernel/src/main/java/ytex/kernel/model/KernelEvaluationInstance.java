package ytex.kernel.model;

import java.io.Serializable;

/**
 * Although there is a many-to-one relationship the KernelEvaluation, we don't
 * model that here - we just use the id so we can batch insert the
 * kernelEvaluations.
 */
public class KernelEvaluationInstance implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int instanceId1;
	private int instanceId2;

	private int kernelEvaluationId;

	private double similarity;

	public KernelEvaluationInstance() {
		super();
	}

	public KernelEvaluationInstance(int kernelEvaluationId, int instanceId1,
			int instanceId2, double similarity) {
		super();
		this.kernelEvaluationId = kernelEvaluationId;
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
		if (kernelEvaluationId != other.kernelEvaluationId)
			return false;
		return true;
	}

	public int getInstanceId1() {
		return instanceId1;
	}

	public int getInstanceId2() {
		return instanceId2;
	}

	public int getKernelEvaluationId() {
		return kernelEvaluationId;
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
		result = prime * result + kernelEvaluationId;
		return result;
	}

	public void setInstanceId1(int instanceId1) {
		this.instanceId1 = instanceId1;
	}

	public void setInstanceId2(int instanceId2) {
		this.instanceId2 = instanceId2;
	}

	public void setKernelEvaluationId(int kernelEvaluationId) {
		this.kernelEvaluationId = kernelEvaluationId;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

}
