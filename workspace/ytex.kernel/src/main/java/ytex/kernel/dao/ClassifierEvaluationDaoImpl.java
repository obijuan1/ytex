package ytex.kernel.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.CorpusEvaluator;
import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.ClassifierEvaluationIRStat;
import ytex.kernel.model.ClassifierInstanceEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureRank;

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
	public void deleteCrossValidationFoldByName(String corpusName,
			String splitName) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCrossValidationFoldByName");
		q.setString("corpusName", corpusName);
		q.setString("splitName", nullToEmptyString(splitName));
		List<CrossValidationFold> folds = q.list();
		for (CrossValidationFold fold : folds)
			this.getSessionFactory().getCurrentSession().delete(fold);
	}

	@Override
	public CrossValidationFold getCrossValidationFold(String corpusName,
			String splitName, String label, int run, int fold) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCrossValidationFold");
		q.setString("corpusName", corpusName);
		q.setString("splitName", nullToEmptyString(splitName));
		q.setString("label", nullToEmptyString(label));
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
		for (FeatureRank r : featureEvaluation.getFeatures())
			this.getSessionFactory().getCurrentSession().save(r);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteFeatureEvaluationByNameAndType(String corpusName,
			String featureSetName, String type) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getFeatureEvaluationByNameAndType");
		q.setString("corpusName", corpusName);
		q.setString("featureSetName", nullToEmptyString(featureSetName));
		q.setString("type", type);
		for (FeatureEvaluation fe : (List<FeatureEvaluation>) q.list())
			this.getSessionFactory().getCurrentSession().delete(fe);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FeatureRank> getTopFeatures(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, String param1, Integer parentConceptTopThreshold) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, "getTopFeatures");
		q.setMaxResults(parentConceptTopThreshold);
		return q.list();
	}

	private Query prepareUniqueFeatureEvalQuery(String corpusName,
			String featureSetName, String label, String evaluationType,
			int foldId, String param1, String queryName) {
		Query q = this.sessionFactory.getCurrentSession().getNamedQuery(
				queryName);
		q.setString("corpusName", corpusName);
		q.setString("featureSetName", nullToEmptyString(featureSetName));
		q.setString("label", nullToEmptyString(label));
		q.setString("evaluationType", evaluationType);
		q.setString("param1", nullToEmptyString(param1));
		q.setInteger("crossValidationFoldId", foldId);
		return q;
	}

	/**
	 * todo for oracle need to handle empty strings differently
	 * 
	 * @param param1
	 * @return
	 */
	private String nullToEmptyString(String param1) {
		return param1 == null ? "" : param1;
	}

	@Override
	public List<FeatureRank> getThresholdFeatures(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, String param1, double evaluationThreshold) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, "getThresholdFeatures");
		q.setDouble("evaluation", evaluationThreshold);
		return null;
	}

	@Override
	public void deleteFeatureEvaluation(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, String param1) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1,
				"getFeatureEvaluationByNK");
		FeatureEvaluation fe = (FeatureEvaluation) q.uniqueResult();
		if (fe != null) {
			// for some reason this isn't working - execute batch updates
			// this.sessionFactory.getCurrentSession().delete(fe);
			q = this.sessionFactory.getCurrentSession().getNamedQuery(
					"deleteFeatureRank");
			q.setInteger("featureEvaluationId", fe.getFeatureEvaluationId());
			q.executeUpdate();
			q = this.sessionFactory.getCurrentSession().getNamedQuery(
					"deleteFeatureEval");
			q.setInteger("featureEvaluationId", fe.getFeatureEvaluationId());
			q.executeUpdate();
		}
	}

	@Override
	public Map<String, Double> getFeatureRankEvaluations(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, String param1) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, "getTopFeatures");
		@SuppressWarnings("unchecked")
		List<FeatureRank> listFeatureRank = q.list();
		Map<String, Double> mapFeatureEval = new HashMap<String, Double>(
				listFeatureRank.size());
		for (FeatureRank r : listFeatureRank) {
			mapFeatureEval.put(r.getFeatureName(), r.getEvaluation());
		}
		return mapFeatureEval;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Object[]> getCorpusCuiTuis(String corpusName,
			String conceptGraphName, String conceptSetName) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, conceptSetName,
				null, CorpusEvaluator.INFOCONTENT, 0, conceptGraphName, "getCorpusCuiTuis");		
		return q.list();
	}

	@Override
	public Map<String, Double> getInfoContent(String corpusName,
			String conceptGraphName, String conceptSet) {
		return getFeatureRankEvaluations(corpusName,
				conceptSet, null, CorpusEvaluator.INFOCONTENT, 0,
				conceptGraphName);
	}
}
