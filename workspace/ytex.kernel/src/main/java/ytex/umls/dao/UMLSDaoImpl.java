package ytex.umls.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class UMLSDaoImpl implements UMLSDao {

	public static final String INCLUDE_REL[] = new String[] { "PAR" };
	public static final String EXCLUDE_RELA[] = new String[] { "inverse_isa" };
	private SessionFactory sessionFactory;
	private static final Log log = LogFactory.getLog(UMLSDaoImpl.class);

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.umls.dao.UMLSDao#getRelationsForSABs(java.util.Set)
	 */
	public List<Object[]> getRelationsForSABs(String[] sabs) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(
				"getRelationsForSABs");
		q.setParameterList("sabs", sabs);
		q.setParameterList("rel", INCLUDE_REL);
		q.setParameterList("relaExclude", EXCLUDE_RELA);
		return (List<Object[]>) q.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.umls.dao.UMLSDao#getAllRelations(java.util.Set)
	 */
	public List<Object[]> getAllRelations() {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(
				"getAllRelations");
		// q.setParameterList("rel", INCLUDE_REL);
		// q.setParameterList("relaExclude", EXCLUDE_RELA);
		return (List<Object[]>) q.list();
	}

}
