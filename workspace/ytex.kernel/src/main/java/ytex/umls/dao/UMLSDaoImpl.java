package ytex.umls.dao;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import ytex.umls.model.UmlsAuiFirstWord;

public class UMLSDaoImpl implements UMLSDao {

	public static final String INCLUDE_REL[] = new String[] { "PAR" };
	public static final String EXCLUDE_RELA[] = new String[] { "inverse_isa" };
	private static final Log log = LogFactory.getLog(UMLSDaoImpl.class);

	SessionFactory sessionFactory;
	private JdbcTemplate t;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setDataSource(DataSource ds) {
		t = new JdbcTemplate(ds);
	}

	public DataSource getDataSource() {
		return t.getDataSource();
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see ytex.umls.dao.UMLSDao#getRelationsForSABs(java.util.Set)
	// */
	// public List<Object[]> getRelationsForSABs(String[] sabs) {
	// Query q = sessionFactory.getCurrentSession().getNamedQuery(
	// "getRelationsForSABs");
	// q.setParameterList("sabs", sabs);
	// // q.setParameterList("rel", INCLUDE_REL);
	// // q.setParameterList("relaExclude", EXCLUDE_RELA);
	// return (List<Object[]>) q.list();
	// }

	// /*
	// * (non-Javadoc)
	// *
	// * @see ytex.umls.dao.UMLSDao#getAllRelations(java.util.Set)
	// */
	// public List<Object[]> getAllRelations() {
	// Query q = sessionFactory.getCurrentSession().getNamedQuery(
	// "getAllRelations");
	// // q.setParameterList("rel", INCLUDE_REL);
	// // q.setParameterList("relaExclude", EXCLUDE_RELA);
	// return (List<Object[]>) q.list();
	// }

	/**
	 * sets up the umls_aui_fword table.
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getAllAuiStr(String lastAui) {
		Query q = null;
		if (lastAui == null)
			q = sessionFactory.getCurrentSession().getNamedQuery(
					"getFirstAuiStr");
		else {
			q = sessionFactory.getCurrentSession().getNamedQuery(
					"getNextAuiStr");
			q.setString("aui", lastAui);
		}
		q.setMaxResults(10000);
		return q.list();
	}

	public void deleteAuiFirstWord() {
		// delete all entries
		sessionFactory.getCurrentSession()
				.createQuery("delete from UmlsAuiFirstWord").executeUpdate();
	}

	public void insertAuiFirstWord(List<UmlsAuiFirstWord> listAuiFirstWord) {
		for (UmlsAuiFirstWord w : listAuiFirstWord)
			sessionFactory.getCurrentSession().save(w);

	}

	@Override
	public Map<String, String> getNames(List<String> subList) {
		Map<String, String> names = new HashMap<String, String>(subList.size());
		// get the shortest string for the specified cuis
		updateNames("getCuiMinStr", subList, names);
		// for those cuis with a preferred name, use it
		updateNames("getCuiPreferredName", subList, names);
		return names;
	}

	private void updateNames(String queryName, List<String> subList,
			Map<String, String> names) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(queryName);
		q.setParameterList("cuis", subList);
		@SuppressWarnings("unchecked")
		List<Object[]> listCuiName = q.list();
		for (Object[] cuiName : listCuiName) {
			names.put((String) cuiName[0], (String) cuiName[1]);
		}
	}

	@Override
	public String getLastAui() {
		Query q = sessionFactory.getCurrentSession()
				.getNamedQuery("getLastAui");
		String aui = (String) q.uniqueResult();
		return aui;
	}

	@Override
	@SuppressWarnings("unchecked")
	public TIntSet getRXNORMCuis() {
		TIntSet cuis = new TIntHashSet();
		for (String cui : (List<String>) sessionFactory.getCurrentSession()
				.getNamedQuery("getRXNORMCuis").list()) {
			Matcher m = UMLSDao.cuiPattern.matcher(cui);
			if (m.find()) {
				cuis.add(Integer.parseInt(m.group(1)));
			}
		}
		return cuis;
	}

	@Override
	public boolean isRXNORMCui(String cui) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(
				"isRXNORMCui");
		q.setCacheable(true);
		q.setString("cui", cui);
		long count = ((Long)q.uniqueResult());
		return count > 0;
	}
}
