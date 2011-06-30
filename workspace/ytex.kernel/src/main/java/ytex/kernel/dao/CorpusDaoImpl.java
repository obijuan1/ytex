package ytex.kernel.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.model.corpus.ConceptInformationContent;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.dao.CorpusDao#updateCorpusTermFrequency(java.lang.String,
	 * java.util.Set)
	 */
	/*
	 * public Corpus updateCorpusTermFrequency(String corpusName, Set<String>
	 * analysisBatches) { // delete corpus terms Query qClearCorpusFrequency =
	 * this.getSessionFactory()
	 * .getCurrentSession().getNamedQuery("clearCorpusFrequency");
	 * qClearCorpusFrequency.setString("corpusName", corpusName);
	 * qClearCorpusFrequency.executeUpdate(); // delete corpus Query
	 * qClearCorpus = this.getSessionFactory().getCurrentSession()
	 * .getNamedQuery("clearCorpus"); qClearCorpus.setString("corpusName",
	 * corpusName); qClearCorpus.executeUpdate(); Corpus c = new Corpus();
	 * c.setCorpusName(corpusName); // create corpus
	 * this.getSessionFactory().getCurrentSession().save(c); // update corpus
	 * frequency Query getTermFrequency =
	 * this.getSessionFactory().getCurrentSession()
	 * .getNamedQuery("getTermFrequency");
	 * getTermFrequency.setParameterList("analysisBatches", analysisBatches);
	 * List<Object[]> listTerms = getTermFrequency.list(); for (Object[]
	 * termFreq : listTerms) { CorpusTerm t = new CorpusTerm(); t.setCorpus(c);
	 * t.setConceptId((String) termFreq[0]); t.setFrequency((Integer)
	 * termFreq[1]); this.getSessionFactory().getCurrentSession().save(t); } //
	 * get total number of terms Query getTotalTermCount =
	 * this.getSessionFactory().getCurrentSession()
	 * .getNamedQuery("getTotalTermCount");
	 * getTotalTermCount.setString("corpusName", corpusName); int total =
	 * (Integer) getTotalTermCount.uniqueResult(); // finalize term counts Query
	 * finalizeCorpusFrequency = this.getSessionFactory()
	 * .getCurrentSession().getNamedQuery("finalizeCorpusFrequency");
	 * finalizeCorpusFrequency.setString("corpusName", corpusName);
	 * finalizeCorpusFrequency.setInteger("total", total);
	 * finalizeCorpusFrequency.executeUpdate(); return c; }
	 */

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
}
