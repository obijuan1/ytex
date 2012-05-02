package ytex.uima.model;

import java.io.Serializable;

/**
 * Mapped to ref_named_entity_regex.
 * @author vijay
 *
 */
public class NamedEntityRegex implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int namedEntityRegexID;
	private String code;
	private String codingScheme;
	private String oid;
	private String regex;
	private String context;
	
	public NamedEntityRegex() {
	}
	
	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public int getNamedEntityRegexID() {
		return namedEntityRegexID;
	}
	public void setNamedEntityRegexID(int namedEntityRegexID) {
		this.namedEntityRegexID = namedEntityRegexID;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCodingScheme() {
		return codingScheme;
	}
	public void setCodingScheme(String codingScheme) {
		this.codingScheme = codingScheme;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + namedEntityRegexID;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NamedEntityRegex))
			return false;
		NamedEntityRegex other = (NamedEntityRegex) obj;
		if (namedEntityRegexID != other.namedEntityRegexID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamedEntityRegex [code=" + code + ", regex=" + regex + "]";
	}
	
}
