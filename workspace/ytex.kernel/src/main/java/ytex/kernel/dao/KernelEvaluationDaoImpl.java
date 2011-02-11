package ytex.kernel.dao;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.model.KernelEvaluation;

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
	public void storeNorm(String name, int instanceId, double norm) {
		storeKernel(name, instanceId, instanceId, norm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dao.KernelEvaluationDao#getNorm(java.lang.String, int)
	 */
	public Double getNorm(String name, int instanceId) {
		return getKernel(name, instanceId, instanceId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dao.KernelEvaluationDao#storeKernel(java.lang.String, int, int,
	 * double)
	 */
	public void storeKernel(String name, int instanceId1, int instanceId2,
			double kernel) {
		int instanceId1s = instanceId1 <= instanceId2 ? instanceId1
				: instanceId2;
		int instanceId2s = instanceId1 <= instanceId2 ? instanceId2
				: instanceId1;
		// delete existing norm
		// if (getKernel(name, instanceId1, instanceId2) != null) {
		Query q = this.getSessionFactory().getCurrentSession().getNamedQuery(
				"deleteKernelEvaluation");
		q.setString("name", name);
		q.setInteger("instanceId1", instanceId1s);
		q.setInteger("instanceId2", instanceId2s);
		q.executeUpdate();
		// if (log.isWarnEnabled())
		// log.warn("replacing kernel, instanceId1: " + instanceId1s
		// + ", instanceId2: " + instanceId2s + ", name: " + name);
		// }
		KernelEvaluation g = new KernelEvaluation(name, instanceId1s,
				instanceId2s, kernel);
		this.getSessionFactory().getCurrentSession().save(g);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dao.KernelEvaluationDao#getKernel(java.lang.String, int, int)
	 */
	public Double getKernel(String name, int instanceId1, int instanceId2) {
		int instanceId1s = instanceId1 <= instanceId2 ? instanceId1
				: instanceId2;
		int instanceId2s = instanceId1 <= instanceId2 ? instanceId2
				: instanceId1;
		Query q = this.getSessionFactory().getCurrentSession().getNamedQuery(
				"getKernelEvaluation");
		q.setCacheable(true);
		q.setString("name", name);
		q.setInteger("instanceId1", instanceId1s);
		q.setInteger("instanceId2", instanceId2s);
		KernelEvaluation g = (KernelEvaluation) q.uniqueResult();
		if (g != null) {
			return g.getSimilarity();
		} else {
			return null;
		}
	}

	@Override
	public List<KernelEvaluation> getAllKernelEvaluationsForInstance(
			Set<String> names, int instanceId) {
		Query q = this.getSessionFactory().getCurrentSession().getNamedQuery(
				"getAllKernelEvaluationsForInstance1");
		q.setParameterList("names", names);
		q.setInteger("instanceId", instanceId);
		List<KernelEvaluation> kevals = q.list();
		Query q2 = this.getSessionFactory().getCurrentSession().getNamedQuery(
				"getAllKernelEvaluationsForInstance2");
		q2.setParameterList("names", names);
		q2.setInteger("instanceId", instanceId);
		kevals.addAll(q2.list());
		return kevals;
	}
}
