package ytex.uima.dao;


import ytex.uima.model.Document;

/**
 * DAO interface for accessing Document objects.
 * @author vijay
 *
 */
public interface DocumentDao {

	/**
	 * Retrieve document by primary key
	 * @param documentID
	 * @return
	 */
	public abstract Document getDocument(int documentID);

//	public abstract Integer saveDocument(JCas jcas, String analysisBatch);

}