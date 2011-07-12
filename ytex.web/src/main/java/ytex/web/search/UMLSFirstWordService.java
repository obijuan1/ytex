package ytex.web.search;


import java.util.List;


/**
 * Dao to get Concepts corresponding to the given text
 * @author vijay
 *
 */
public interface UMLSFirstWordService {

	/**
	 * get Concepts that start with the specified text.
	 * @param fword
	 * @return
	 */
	public abstract List<UMLSFirstWord> getUMLSbyFirstWord(String fword);

}