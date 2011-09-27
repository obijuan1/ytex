package ytex.kernel.dao;

import java.util.List;

import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.model.KernelEvaluationInstance;

public interface KernelEvaluationDao {

	public abstract void storeNorm(KernelEvaluation kernelEvaluation,
			long instanceId, double norm);

	public abstract Double getNorm(KernelEvaluation kernelEvaluation,
			long instanceId);

	public abstract void storeKernel(KernelEvaluation kernelEvaluation,
			long instanceId1, long instanceId2, double kernel);

	public abstract Double getKernel(KernelEvaluation kernelEvaluation,
			long instanceId1, long instanceId2);

	public List<KernelEvaluationInstance> getAllKernelEvaluationsForInstance(
			KernelEvaluation kernelEvaluation, long instanceId);

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
			String label, int foldId, double param1, String param2);

}