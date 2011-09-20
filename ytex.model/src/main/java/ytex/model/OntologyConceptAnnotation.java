package ytex.model;

import java.io.Serializable;

/**
 * Mapped to cTAKES OntologyConcept
 * @author vijay
 *
 */
public class OntologyConceptAnnotation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int ontologyConceptAnnotationID;
	String codingScheme;
	String code;
	String oid;
	NamedEntityAnnotation namedEntityAnnotation;

	public int getOntologyConceptAnnotationID() {
		return ontologyConceptAnnotationID;
	}

	public void setOntologyConceptAnnotationID(int ontologyConceptAnnotationID) {
		this.ontologyConceptAnnotationID = ontologyConceptAnnotationID;
	}

	public String getCodingScheme() {
		return codingScheme;
	}

	public void setCodingScheme(String codingScheme) {
		this.codingScheme = codingScheme;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public NamedEntityAnnotation getNamedEntityAnnotation() {
		return namedEntityAnnotation;
	}

	public void setNamedEntityAnnotation(
			NamedEntityAnnotation namedEntityAnnotation) {
		this.namedEntityAnnotation = namedEntityAnnotation;
	}

	@Override
	public String toString() {
		return "OntologyConceptAnnotation [code=" + code + ", codingScheme="
				+ codingScheme + ", oid=" + oid + ", toString()="
				+ super.toString() + "]";
	}

	public OntologyConceptAnnotation() {
		super();
	}

	public OntologyConceptAnnotation(NamedEntityAnnotation ne) {
		super();
		this.namedEntityAnnotation = ne;
//		this.code = annotation.getCode();
//		this.codingScheme = annotation.getCodingScheme();
//		this.oid = annotation.getOid();
	}
}
