package ytex.model;

import java.io.Serializable;

/**
 * Mapped to ref_segment_regex
 * @author vijay
 *
 */
public class SegmentRegex implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int segmentRegexID;
	String regex;
	String segmentID;
	boolean limitToRegex;
	public boolean isLimitToRegex() {
		return limitToRegex;
	}
	public void setLimitToRegex(boolean limitToRegex) {
		this.limitToRegex = limitToRegex;
	}
	public int getSegmentRegexID() {
		return segmentRegexID;
	}
	public void setSegmentRegexID(int segmentRegexID) {
		this.segmentRegexID = segmentRegexID;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public String getSegmentID() {
		return segmentID;
	}
	public void setSegmentID(String segmentID) {
		this.segmentID = segmentID;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + segmentRegexID;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SegmentRegex other = (SegmentRegex) obj;
		if (segmentRegexID != other.segmentRegexID)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SegmentRegex [regex=" + regex + ", segmentID=" + segmentID
				+ ", segmentRegexID=" + segmentRegexID + "]";
	}
	public SegmentRegex() {
		super();
	}

}
