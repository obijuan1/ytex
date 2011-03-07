package ytex.kernel.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import ytex.kernel.model.Corpus;
import ytex.kernel.model.CorpusTerm;
import ytex.kernel.model.InfoContent;

public class CorpusDaoImpl implements CorpusDao {
	private SessionFactory sessionFactory;
	private static final Log log = LogFactory.getLog(CorpusDaoImpl.class);

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
	public List<CorpusTerm> getTerms(String corpusName) {
		Query getTerms = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getTerms");
		getTerms.setString("corpusName", corpusName);
		return getTerms.list();
	}

	public void addInfoContent(InfoContent infoContent) {
		this.getSessionFactory().getCurrentSession().save(infoContent);
	}

	public Corpus getCorpus(String corpusName) {
		Query getTerms = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getCorpus");
		getTerms.setString("corpusName", corpusName);
		return (Corpus) getTerms.uniqueResult();
	}

	public List<InfoContent> getInfoContent(List<String> corpusNames) {
		Query getTerms = this.getSessionFactory().getCurrentSession()
				.getNamedQuery("getInfoContent");
		getTerms.setParameterList("corpusNames", corpusNames);
		return getTerms.list();
	}
	
	public List<Object[]> getCorpusCuiTuis(String corpusName) {
		Query getCorpusCuiTuis = this.getSessionFactory().getCurrentSession().getNamedQuery("getCorpusCuiTuis");
		getCorpusCuiTuis.setString("corpusName", corpusName);
		return getCorpusCuiTuis.list();
	}
}
