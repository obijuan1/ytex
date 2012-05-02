package ytex.uima.dao;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

import ytex.uima.model.Document;

public class DocumentDaoImpl implements DocumentDao {
	private SessionFactory sessionFactory;
	private static final Log log = LogFactory.getLog(DocumentDaoImpl.class);
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.va.vacs.esld.dao.DocumentDao#getDocument(int)
	 */
	public Document getDocument(int documentID) {
		return (Document) this.sessionFactory.getCurrentSession().get(
				Document.class, documentID);
	}

}
