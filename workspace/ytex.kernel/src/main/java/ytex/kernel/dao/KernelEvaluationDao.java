package ytex.kernel.dao;

import java.util.List;
import java.util.Set;

import ytex.kernel.model.KernelEvaluation;

public interface KernelEvaluationDao {

	public abstract void storeNorm(String name, int instanceId, double norm);

	public abstract Double getNorm(String name, int instanceId);

	public abstract void storeKernel(String name, int instanceId1,
			int instanceId2, double kernel);

	public abstract Double getKernel(String name, int instanceId1,
			int instanceId2);

	public List<KernelEvaluation> getAllKernelEvaluationsForInstance(
			Set<String> names, int instanceId);
	
}