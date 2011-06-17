package ytex.kernel.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.ClassifierEvaluationIRStat;
import ytex.kernel.model.ClassifierInstanceEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.FeatureEvaluation;

public class ClassifierEvaluationDaoImpl implements ClassifierEvaluationDao {
	private static final Log log = LogFactory
			.getLog(ClassifierEvaluationDaoImpl.class);
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteCrossValidationFoldByName(String name) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCrossValidationFoldByName");
		q.setString("name", name);
		List<CrossValidationFold> folds = q.list();
		for (CrossValidationFold fold : folds)
			this.getSessionFactory().getCurrentSession().delete(fold);
	}

	@Override
	public CrossValidationFold getCrossValidationFold(String name,
			String label, int run, int fold) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCrossValidationFold");
		q.setString("name", name);
		q.setString("label", label);
		q.setInteger("run", run);
		q.setInteger("fold", fold);
		return (CrossValidationFold) q.uniqueResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.dao.ClassifierEvaluationDao#saveClassifierEvaluation(ytex
	 * .kernel.model.ClassifierEvaluation)
	 */
	public void saveClassifierEvaluation(ClassifierEvaluation eval,
			boolean saveInstanceEval) {
		saveClassifierEvaluation(eval, saveInstanceEval, true, null);
	}

	public void saveClassifierEvaluation(ClassifierEvaluation eval,
			boolean saveInstanceEval, boolean saveIRStats,
			Integer excludeTargetClassId) {
		this.getSessionFactory().getCurrentSession().save(eval);
		if (saveIRStats)
			this.saveIRStats(eval, excludeTargetClassId);
		if (saveInstanceEval) {
			for (ClassifierInstanceEvaluation instanceEval : eval
					.getClassifierInstanceEvaluations().values()) {
				this.getSessionFactory().getCurrentSession().save(instanceEval);
			}
		}
	}

	void saveIRStats(ClassifierEvaluation eval, Integer excludeTargetClassId) {
		Set<Integer> classIds = this.getClassIds(eval, excludeTargetClassId);
		// setup stats
		for (Integer irClassId : classIds) {
			ClassifierEvaluationIRStat irStat = calcIRStats(irClassId, eval,
					excludeTargetClassId);
			this.getSessionFactory().getCurrentSession().save(irStat);
			eval.getClassifierIRStats().put(irClassId, irStat);
		}
	}

	/**
	 * 
	 * @param irClassId
	 *            the target class id with respect to ir statistics will be
	 *            calculated
	 * @param eval
	 *            the object to update
	 * @param excludeTargetClassId
	 *            class id to be excluded from computation of ir stats.
	 * @return
	 */
	private ClassifierEvaluationIRStat calcIRStats(Integer irClassId,
			ClassifierEvaluation eval, Integer excludeTargetClassId) {
		int tp = 0;
		int tn = 0;
		int fp = 0;
		int fn = 0;
		for (ClassifierInstanceEvaluation instanceEval : eval
				.getClassifierInstanceEvaluations().values()) {

			if (instanceEval.getTargetClassId() != null
					&& (excludeTargetClassId == null || instanceEval
							.getTargetClassId() != excludeTargetClassId
							.intValue())) {
				if (instanceEval.getTargetClassId() == irClassId) {
					if (instanceEval.getPredictedClassId() == instanceEval
							.getTargetClassId()) {
						tp++;
					} else {
						fn++;
					}
				} else {
					if (instanceEval.getPredictedClassId() == irClassId) {
						fp++;
					} else {
						tn++;
					}
				}
			}
		}
		return new ClassifierEvaluationIRStat(eval, irClassId, tp, tn, fp, fn);
	}

	private Set<Integer> getClassIds(ClassifierEvaluation eval,
			Integer excludeTargetClassId) {
		Set<Integer> classIds = new HashSet<Integer>();
		for (ClassifierInstanceEvaluation instanceEval : eval
				.getClassifierInstanceEvaluations().values()) {
			classIds.add(instanceEval.getPredictedClassId());
			if (instanceEval.getTargetClassId() != null
					&& (excludeTargetClassId == null || instanceEval
							.getTargetClassId() != excludeTargetClassId
							.intValue()))
				classIds.add(instanceEval.getTargetClassId());
		}
		return classIds;
	}

	@Override
	public void saveFold(CrossValidationFold fold) {
		this.getSessionFactory().getCurrentSession().save(fold);
	}

	// @Override
	// public void saveInfogain(List<FeatureInfogain> foldInfogainList) {
	// for(FeatureInfogain ig : foldInfogainList) {
	// this.getSessionFactory().getCurrentSession().save(ig);
	// }
	// }

	@Override
	public void saveFeatureEvaluation(FeatureEvaluation featureEvaluation) {
		this.getSessionFactory().getCurrentSession().save(featureEvaluation);
	}

	@Override
	public void deleteFeatureEvaluationByNameAndType(String name, String type) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getFeatureEvaluationByNameAndType");
		q.setString("name", name);
		q.setString("type", type);
		for (FeatureEvaluation fe : (List<FeatureEvaluation>) q.list())
			this.getSessionFactory().getCurrentSession().delete(fe);
	}

}
