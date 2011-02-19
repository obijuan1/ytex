package ytex.kernel.model;

import java.io.Serializable;

public class KernelEvaluation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	int kernelEvaluationId;
	String name;
	int instanceId1;
	int instanceId2;
	double similarity;

//	public int getKernelEvaluationId() {
//		return kernelEvaluationId;
//	}
//
//	public void setKernelEvaluationId(int kernelEvaluationId) {
//		this.kernelEvaluationId = kernelEvaluationId;
//	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getInstanceId1() {
		return instanceId1;
	}

	public void setInstanceId1(int instanceId1) {
		this.instanceId1 = instanceId1;
	}

	public int getInstanceId2() {
		return instanceId2;
	}

	public void setInstanceId2(int instanceId2) {
		this.instanceId2 = instanceId2;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + instanceId1;
		result = prime * result + instanceId2;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KernelEvaluation other = (KernelEvaluation) obj;
		if (instanceId1 != other.instanceId1)
			return false;
		if (instanceId2 != other.instanceId2)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public KernelEvaluation() {
		super();
	}

	public KernelEvaluation(String name, int instanceId1, int instanceId2,
			double similarity) {
		super();
		this.name = name;
		this.instanceId1 = instanceId1;
		this.instanceId2 = instanceId2;
		this.similarity = similarity;
	}

}
