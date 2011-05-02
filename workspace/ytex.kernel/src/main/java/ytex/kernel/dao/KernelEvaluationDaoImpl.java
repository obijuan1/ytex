package ytex.kernel.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.model.KernelEvaluationInstance;

public class KernelEvaluationDaoImpl implements KernelEvaluationDao {
	private SessionFactory sessionFactory;
	private static final Log log = LogFactory
			.getLog(KernelEvaluationDaoImpl.class);

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dao.KernelEvaluationDao#storeNorm(java.lang.String, int, double)
	 */
	public void storeNorm(KernelEvaluation kernelEvaluation, int instanceId,
			double norm) {
		storeKernel(kernelEvaluation, instanceId, instanceId, norm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dao.KernelEvaluationDao#getNorm(java.lang.String, int)
	 */
	public Double getNorm(KernelEvaluation kernelEvaluation, int instanceId) {
		return getKernel(kernelEvaluation, instanceId, instanceId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dao.KernelEvaluationDao#storeKernel(java.lang.String, int, int,
	 * double)
	 */
	public void storeKernel(KernelEvaluation kernelEvaluation, int instanceId1,
			int instanceId2, double kernel) {
		int instanceId1s = instanceId1 <= instanceId2 ? instanceId1
				: instanceId2;
		int instanceId2s = instanceId1 <= instanceId2 ? instanceId2
				: instanceId1;
		// delete existing norm
		// if (getKernel(name, instanceId1, instanceId2) != null) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("deleteKernelEvaluation");
		q.setInteger("kernelEvaluationId",
				kernelEvaluation.getKernelEvaluationId());
		q.setInteger("instanceId1", instanceId1s);
		q.setInteger("instanceId2", instanceId2s);
		q.executeUpdate();
		// if (log.isWarnEnabled())
		// log.warn("replacing kernel, instanceId1: " + instanceId1s
		// + ", instanceId2: " + instanceId2s + ", name: " + name);
		// }
		KernelEvaluationInstance g = new KernelEvaluationInstance(
				kernelEvaluation, instanceId1s, instanceId2s, kernel);
		this.getSessionFactory().getCurrentSession().save(g);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dao.KernelEvaluationDao#getKernel(java.lang.String, int, int)
	 */
	public Double getKernel(KernelEvaluation kernelEvaluation, int instanceId1,
			int instanceId2) {
		int instanceId1s = instanceId1 <= instanceId2 ? instanceId1
				: instanceId2;
		int instanceId2s = instanceId1 <= instanceId2 ? instanceId2
				: instanceId1;
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getKernelEvaluation");
		q.setCacheable(true);
		q.setInteger("kernelEvaluationId",
				kernelEvaluation.getKernelEvaluationId());
		q.setInteger("instanceId1", instanceId1s);
		q.setInteger("instanceId2", instanceId2s);
		KernelEvaluationInstance g = (KernelEvaluationInstance) q
				.uniqueResult();
		if (g != null) {
			return g.getSimilarity();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KernelEvaluationInstance> getAllKernelEvaluationsForInstance(
			KernelEvaluation kernelEvaluation, int instanceId) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getAllKernelEvaluationsForInstance1");
		q.setInteger("kernelEvaluationId",
				kernelEvaluation.getKernelEvaluationId());
		q.setInteger("instanceId", instanceId);
		List<KernelEvaluationInstance> kevals = q.list();
		Query q2 = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getAllKernelEvaluationsForInstance2");
		q2.setInteger("kernelEvaluationId",
				kernelEvaluation.getKernelEvaluationId());
		q2.setInteger("instanceId", instanceId);
		kevals.addAll(q2.list());
		return kevals;
	}

	@Override
	public KernelEvaluation storeKernelEval(KernelEvaluation kernelEvaluation) {
		KernelEvaluation kEval = getKernelEval(kernelEvaluation.getName(),
				kernelEvaluation.getExperiment(), kernelEvaluation.getLabel(),
				kernelEvaluation.getFoldId());
		if (kEval == null) {
			try {
				this.getSessionFactory().getCurrentSession()
						.save(kernelEvaluation);
				kEval = kernelEvaluation;
			} catch (Exception e) {
				log.warn(
						"couldn't save kernel evaluation, maybe somebody else did. try to retrieve kernel eval",
						e);
				kEval = getKernelEval(kernelEvaluation.getName(),
						kernelEvaluation.getExperiment(),
						kernelEvaluation.getLabel(),
						kernelEvaluation.getFoldId());
			}
		}
		return kEval;
	}

	public KernelEvaluation getKernelEval(String name, String experiment,
			String label, int foldId) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getKernelEval");
		q.setString("name", name);
		q.setString("experiment", experiment);
		q.setString("label", label);
		q.setInteger("foldId", foldId);
		return (KernelEvaluation) q.uniqueResult();
	}
}
