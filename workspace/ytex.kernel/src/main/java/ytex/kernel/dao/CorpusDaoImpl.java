package ytex.kernel.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.model.corpus.ConceptInformationContent;
import ytex.kernel.model.corpus.ConceptLabelChild;
import ytex.kernel.model.corpus.ConceptLabelStatistic;
import ytex.kernel.model.corpus.CorpusEvaluation;
import ytex.kernel.model.corpus.CorpusLabelEvaluation;

public class CorpusDaoImpl implements CorpusDao {
	private SessionFactory sessionFactory;

	// private static final Log log = LogFactory.getLog(CorpusDaoImpl.class);

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public Map<String, Double> getInfoContent(String corpusName,
			String conceptGraphName, String conceptSet) {
		Query getTerms = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getInfoContent");
		getTerms.setString("corpusName", corpusName);
		getTerms.setString("conceptGraphName", conceptGraphName);
		getTerms.setString("conceptSetName", conceptSet);
		Map<String, Double> corpusIC = new HashMap<String, Double>();
		for (Object objIC : getTerms.list()) {
			ConceptInformationContent ic = (ConceptInformationContent) objIC;
			corpusIC.put(ic.getConceptId(), ic.getInformationContent());
		}
		return corpusIC;
	}

	public void addInfoContent(ConceptInformationContent infoContent) {
		this.getSessionFactory().getCurrentSession().save(infoContent);
	}

	public CorpusEvaluation getCorpus(String corpusName,
			String conceptGraphName, String conceptSetName) {
		Query getCorpus = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCorpus");
		getCorpus.setString("corpusName", corpusName);
		getCorpus.setString("conceptGraphName", conceptGraphName);
		getCorpus.setString("conceptSetName", conceptSetName);
		return (CorpusEvaluation) getCorpus.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getCorpusCuiTuis(String corpusName,
			String conceptGraphName, String conceptSetName) {
		Query getCorpusCuiTuis = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCorpusCuiTuis");
		getCorpusCuiTuis.setString("corpusName", corpusName);
		getCorpusCuiTuis.setString("conceptGraphName", conceptGraphName);
		getCorpusCuiTuis.setString("conceptSetName", conceptSetName);
		return getCorpusCuiTuis.list();
	}

	@Override
	public void addCorpus(CorpusEvaluation eval) {
		this.getSessionFactory().getCurrentSession().save(eval);
	}

	@Override
	public void addCorpusLabelEval(CorpusLabelEvaluation eval) {
		this.getSessionFactory().getCurrentSession().save(eval);
	}

	@Override
	public void addLabelStatistic(ConceptLabelStatistic labelStatistic) {
		this.getSessionFactory().getCurrentSession().save(labelStatistic);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ConceptLabelStatistic> getLabelStatistic(String corpusName,
			String conceptGraphName, String conceptSetName, String label,
			Integer foldId) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getLabelStatistic");
		setCorpusLabelQueryParams(corpusName, conceptGraphName, conceptSetName,
				label, foldId, q);
		return q.list();
	}

	private void setCorpusLabelQueryParams(String corpusName,
			String conceptGraphName, String conceptSetName, String label,
			Integer foldId, Query q) {
		q.setString("corpusName", corpusName);
		q.setString("conceptGraphName", conceptGraphName);
		q.setString("conceptSetName", conceptSetName);
		q.setString("label", label);
		if (foldId != null)
			q.setInteger("foldId", foldId);
		else
			q.setParameter("foldId", null, Hibernate.INTEGER);
	}

	@Override
	public CorpusLabelEvaluation getCorpusLabelEvaluation(String corpusName,
			String conceptGraphName, String conceptSetName, String label,
			Integer foldId) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCorpusLabelEvaluation");
		setCorpusLabelQueryParams(corpusName, conceptGraphName, conceptSetName,
				label, foldId, q);
		return (CorpusLabelEvaluation) q.uniqueResult();
	}

	@Override
	public void saveConceptLabelChildren(Collection<ConceptLabelChild> values) {
		for (ConceptLabelChild chd : values) {
			this.sessionFactory.getCurrentSession().save(chd);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ConceptLabelStatistic> getTopCorpusLabelStat(
			CorpusLabelEvaluation labelEval, Integer parentConceptThreshold) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getTopCorpusLabelStat");
		q.setInteger("corpusConceptLabelEvaluationId",
				labelEval.getCorpusConceptLabelEvaluationId());
		q.setMaxResults(parentConceptThreshold);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ConceptLabelStatistic> getThresholdCorpusLabelStat(
			CorpusLabelEvaluation labelEval,
			Double parentConceptMutualInfoThreshold) {
		Query q = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getThresholdCorpusLabelStat");
		q.setInteger("corpusConceptLabelEvaluationId",
				labelEval.getCorpusConceptLabelEvaluationId());
		q.setDouble("mutualInfo", parentConceptMutualInfoThreshold);
		return q.list();
	}
}
