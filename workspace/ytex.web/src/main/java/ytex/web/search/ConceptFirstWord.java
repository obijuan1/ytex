package ytex.web.search;

import java.io.Serializable;

/**
 * Mapped to V_UMLS_FWORD_LOOKUP
 * This table does not have a primary key, so we can't map it with hibernate.
 * We could add a primary key, but the lookups are fairly simple, so
 * we do it with sql.
 * 
 * @author vijay
 */
public class ConceptFirstWord implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fword;
	private String conceptId;
	private String text;
	public String getFword() {
		return fword;
	}
	public void setFword(String fword) {
		this.fword = fword;
	}
	public String getConceptId() {
		return conceptId;
	}
	public void setConceptId(String cui) {
		this.conceptId = cui;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	@Override
	public String toString() {
		return "UMLSFirstWord [cui=" + conceptId + ", text=" + text + "]";
	}
	

}
