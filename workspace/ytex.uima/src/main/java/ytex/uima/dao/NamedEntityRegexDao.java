package ytex.uima.dao;


import java.util.List;

import ytex.uima.model.NamedEntityRegex;

/**
 * Dao to access NamedEntity Regular Expressions used by the NamedEntityRegexAnnotator
 * @author vijay
 *
 */
public interface NamedEntityRegexDao {

	public abstract List<NamedEntityRegex> getNamedEntityRegexs();

}