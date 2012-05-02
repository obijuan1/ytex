package ytex.uima.dao;


import java.util.List;

import org.hibernate.SessionFactory;

import ytex.uima.model.SegmentRegex;

public class SegmentRegexDaoImpl implements SegmentRegexDao {
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	private SessionFactory sessionFactory;
	/* (non-Javadoc)
	 * @see gov.va.vacs.esld.dao.SegmentRegex#getSegmentRegexs()
	 */
	@SuppressWarnings("unchecked")
	public List<SegmentRegex> getSegmentRegexs() {
		return (List<SegmentRegex>)sessionFactory.getCurrentSession().createQuery("from SegmentRegex").list();
	}
}
