package ytex.weka;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

public class DocumentResultInstanceImporter implements
		WekaResultInstanceImporter {
	private SessionFactory sessionFactory;
	private static final Log log = LogFactory
			.getLog(DocumentResultInstanceImporter.class);

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void importInstanceResult(Integer instanceNumber,
			List<String> instanceKey, String task, int classAuto,
			int classGold, List<Double> predictions) {
		// if (instanceKey.size() < 1) {
		try {
			int documentId = Integer.parseInt(instanceKey.get(0));
			if (documentId > 0) {
				// todo fix this
//				Document doc = (Document) this.getSessionFactory()
//						.getCurrentSession().get(Document.class, documentId);
//				if (doc != null) {
//					DocumentClass docClass = new DocumentClass();
//					docClass.setDocument(doc);
//					docClass.setClassAuto(classAuto);
//					docClass.setClassGold(classGold);
//					docClass.setTask(task);
//					this.getSessionFactory().getCurrentSession().save(docClass);
//				} else {
//					log.error("no document for id: " + documentId);
//				}
			} else {
				log.error("Invalid instance id: " + instanceKey
						+ ", instanceNumber: " + instanceNumber);
			}
		} catch (NumberFormatException nfe) {
			log.error("could not parse document id: " + instanceKey
					+ ", instanceNumber: " + instanceNumber, nfe);
		}
		// } else {
		// log.error("no attributes in key, instanceNumber: " + instanceNumber);
		// }
	}
}
