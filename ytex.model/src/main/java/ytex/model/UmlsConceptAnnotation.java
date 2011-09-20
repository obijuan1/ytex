package ytex.model;

/**
 * Mapped to cTAKES UmlsConcept
 * @author vijay
 *
 */
public class UmlsConceptAnnotation extends OntologyConceptAnnotation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String cui;

	@Override
	public String toString() {
		return "UmlsConceptAnnotation [cui=" + cui + ", toString()="
				+ super.toString() + "]";
	}

	public UmlsConceptAnnotation() {
		super();
	}

	public UmlsConceptAnnotation(NamedEntityAnnotation ne) {
		super(ne);
//		this.cui = annotation.getCui();
	}

	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}
}
