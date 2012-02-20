package ytex.web.search;

import java.util.List;

/**
 * Dao to get Concepts corresponding to the given text
 * 
 * @author vijay
 * 
 */
public interface ConceptSearchService {

	/**
	 * get Concepts that start with the specified text.
	 * 
	 * @param fword
	 * @return
	 */
	public abstract List<ConceptFirstWord> getConceptByFirstWord(String fword);

	public String getTermByConceptId(String conceptId);

	/**
	 * if the conceptId is a valid conceptId, get the corresponding term
	 * 
	 * @param conceptId
	 * @return term
	 */
	public abstract String checkTermByConceptId(String conceptId);

}