package ytex.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import ytex.model.NamedEntityRegex;


public class NamedEntityRegexDaoImpl implements NamedEntityRegexDao {
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	private SessionFactory sessionFactory;
	/* (non-Javadoc)
	 * @see gov.va.vacs.esld.dao.NamedEntityRegexDao#getNamedEntityRegexs()
	 */
	@SuppressWarnings("unchecked")
	public List<NamedEntityRegex> getNamedEntityRegexs() {
		return (List<NamedEntityRegex>)sessionFactory.getCurrentSession().createQuery("from NamedEntityRegex").list();
	}

}
