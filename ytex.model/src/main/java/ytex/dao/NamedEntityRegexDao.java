package ytex.dao;


import java.util.List;

import ytex.model.NamedEntityRegex;

/**
 * Dao to access NamedEntity Regular Expressions used by the NamedEntityRegexAnnotator
 * @author vijay
 *
 */
public interface NamedEntityRegexDao {

	public abstract List<NamedEntityRegex> getNamedEntityRegexs();

}