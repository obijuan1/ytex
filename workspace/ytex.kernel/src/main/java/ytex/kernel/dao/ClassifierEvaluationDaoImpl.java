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

import ytex.dao.DBUtil;
import ytex.kernel.InfoContentEvaluator;
import ytex.kernel.IntrinsicInfoContentEvaluator;
import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.ClassifierEvaluationIRStat;
import ytex.kernel.model.ClassifierInstanceEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureParentChild;
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
			Map<Integer, String> irClassMap, boolean saveInstanceEval) {
		saveClassifierEvaluation(eval, irClassMap, saveInstanceEval, true, null);
	}

	public void saveClassifierEvaluation(ClassifierEvaluation eval,
			Map<Integer, String> irClassMap, boolean saveInstanceEval,
			boolean saveIRStats, Integer excludeTargetClassId) {
		this.getSessionFactory().getCurrentSession().save(eval);
		if (saveIRStats)
			this.saveIRStats(eval, irClassMap, excludeTargetClassId);
		if (saveInstanceEval) {
			for (ClassifierInstanceEvaluation instanceEval : eval
					.getClassifierInstanceEvaluations().values()) {
				this.getSessionFactory().getCurrentSession().save(instanceEval);
			}
		}
	}

	void saveIRStats(ClassifierEvaluation eval,
			Map<Integer, String> irClassMap, Integer excludeTargetClassId) {
		Set<Integer> classIds = this.getClassIds(eval, excludeTargetClassId);
		// setup stats
		for (Integer irClassId : classIds) {
			String irClass = null;
			if (irClassMap != null)
				irClass = irClassMap.get(irClassId);
			if (irClass == null)
				irClass = Integer.toString(irClassId);
			ClassifierEvaluationIRStat irStat = calcIRStats(irClass, irClassId,
					eval, excludeTargetClassId);
			this.getSessionFactory().getCurrentSession().save(irStat);
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
	private ClassifierEvaluationIRStat calcIRStats(String irClass,
			Integer irClassId, ClassifierEvaluation eval,
			Integer excludeTargetClassId) {
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
		return new ClassifierEvaluationIRStat(eval, null, irClass, irClassId,
				tp, tn, fp, fn);
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
	public void saveFeatureEvaluation(FeatureEvaluation featureEvaluation,
			List<FeatureRank> features) {
		this.getSessionFactory().getCurrentSession().save(featureEvaluation);
		for (FeatureRank r : features)
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
			Integer foldId, double param1, String param2,
			Integer parentConceptTopThreshold) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, param2, "getTopFeatures");
		q.setMaxResults(parentConceptTopThreshold);
		return q.list();
	}
	
	@Override
	public Double getMaxFeatureEvaluation(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, double param1, String param2) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, param2, "getMaxFeatureEvaluation");
		return (Double)q.uniqueResult();
	}

	private Query prepareUniqueFeatureEvalQuery(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, Double param1, String param2, String queryName) {
		Query q = this.sessionFactory.getCurrentSession().getNamedQuery(
				queryName);
		q.setString("corpusName", nullToEmptyString(corpusName));
		q.setString("featureSetName", nullToEmptyString(featureSetName));
		q.setString("label", nullToEmptyString(label));
		q.setString("evaluationType", evaluationType);
		q.setDouble("param1", param1 == null ? 0 : param1);
		q.setString("param2", nullToEmptyString(param2));
		q.setInteger("crossValidationFoldId", foldId == null ? 0 : foldId);
		return q;
	}

	/**
	 * todo for oracle need to handle empty strings differently
	 * 
	 * @param param1
	 * @return
	 */
	private String nullToEmptyString(String param1) {
		return DBUtil.nullToEmptyString(param1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FeatureRank> getThresholdFeatures(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, double param1, String param2,
			double evaluationThreshold) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, param2,
				"getThresholdFeatures");
		q.setDouble("evaluation", evaluationThreshold);
		return q.list();
	}

	@Override
	public void deleteFeatureEvaluation(String corpusName,
			String featureSetName, String label, String evaluationType,
			Integer foldId, Double param1, String param2) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, param2,
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
			Integer foldId, double param1, String param2) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, featureSetName,
				label, evaluationType, foldId, param1, param2, "getTopFeatures");
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
				null, InfoContentEvaluator.INFOCONTENT, 0, 0d,
				conceptGraphName, "getCorpusCuiTuis");
		return q.list();
	}

	@Override
	public Map<String, Double> getInfoContent(String corpusName,
			String conceptGraphName, String conceptSet) {
		return getFeatureRankEvaluations(corpusName, conceptSet, null,
				InfoContentEvaluator.INFOCONTENT, 0, 0, conceptGraphName);
	}

	@Override
	public Map<String, Double> getIntrinsicInfoContent(String conceptGraphName) {
		return getFeatureRankEvaluations(null, null, null,
				IntrinsicInfoContentEvaluator.INTRINSIC_INFOCONTENT, 0, 0, conceptGraphName);
	}

	@Override
	public void saveFeatureParentChild(FeatureParentChild parchd) {
		this.sessionFactory.getCurrentSession().save(parchd);
	}

	@Override
	public List<FeatureRank> getImputedFeaturesByPropagatedCutoff(
			String corpusName, String conceptSetName, String label,
			String evaluationType, String conceptGraphName,
			String propEvaluationType, int propRankCutoff) {
		Query q = prepareUniqueFeatureEvalQuery(corpusName, conceptSetName,
				label, evaluationType, 0, 0d, conceptGraphName,
				"getImputedFeaturesByPropagatedCutoff");
		q.setInteger("propRankCutoff", propRankCutoff);
		q.setString("propEvaluationType", propEvaluationType);
		return q.list();
	}
}
