package ytex.kernel.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.ClassifierInstanceEvaluation;

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

}
