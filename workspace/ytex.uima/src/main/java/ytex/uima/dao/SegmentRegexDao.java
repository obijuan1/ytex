package ytex.uima.dao;


import java.util.List;

import ytex.uima.model.SegmentRegex;

/**
 * Dao to access Segment Boundary Regular Expressions.
 * Used by SegmentRegexAnnotator.
 * @author vijay
 *
 */
public interface SegmentRegexDao {

	/* (non-Javadoc)
	 * @see gov.va.vacs.esld.dao.NamedEntityRegexDao#getNamedEntityRegexs()
	 */
	public abstract List<SegmentRegex> getSegmentRegexs();

}