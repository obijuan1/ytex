package ytex.kernel.dao;

import java.util.List;

import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.model.KernelEvaluationInstance;

public interface KernelEvaluationDao {

	public abstract void storeNorm(KernelEvaluation kernelEvaluation,
			int instanceId, double norm);

	public abstract Double getNorm(KernelEvaluation kernelEvaluation,
			int instanceId);

	public abstract void storeKernel(KernelEvaluation kernelEvaluation,
			int instanceId1, int instanceId2, double kernel);

	public abstract Double getKernel(KernelEvaluation kernelEvaluation,
			int instanceId1, int instanceId2);

	public List<KernelEvaluationInstance> getAllKernelEvaluationsForInstance(
			KernelEvaluation kernelEvaluation, int instanceId);

	/**
	 * store the kernel evaluation if it doesn't exist, else return the existing
	 * one
	 * 
	 * @param kernelEvaluation
	 * @return
	 */
	public abstract KernelEvaluation storeKernelEval(
			KernelEvaluation kernelEvaluation);

	public abstract KernelEvaluation getKernelEval(String name, String experiment,
			String label, int foldId);

}