package ytex.kernel.dao;

import java.util.List;
import java.util.Set;

import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.model.KernelEvaluationInstance;

public interface KernelEvaluationDao {

	public abstract void storeNorm(KernelEvaluation kernelEvaluation, int instanceId, double norm);

	public abstract Double getNorm(KernelEvaluation kernelEvaluation, int instanceId);

	public abstract void storeKernel(KernelEvaluation kernelEvaluation, int instanceId1,
			int instanceId2, double kernel);

	public abstract Double getKernel(KernelEvaluation kernelEvaluation, int instanceId1,
			int instanceId2);

	public List<KernelEvaluationInstance> getAllKernelEvaluationsForInstance(
			KernelEvaluation kernelEvaluation, int instanceId);
	
}