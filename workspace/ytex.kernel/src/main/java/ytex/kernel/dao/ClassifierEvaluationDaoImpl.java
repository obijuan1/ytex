package ytex.kernel.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.ClassifierInstanceEvaluation;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureInfogain;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.dao.ClassifierEvaluationDao#saveClassifierEvaluation(ytex
	 * .kernel.model.ClassifierEvaluation)
	 */
	public void saveClassifierEvaluation(ClassifierEvaluation eval) {
		this.getSessionFactory().getCurrentSession().save(eval);
		for (ClassifierInstanceEvaluation instanceEval : eval
				.getClassifierInstanceEvaluations().values()) {
			this.getSessionFactory().getCurrentSession().save(instanceEval);
		}
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
