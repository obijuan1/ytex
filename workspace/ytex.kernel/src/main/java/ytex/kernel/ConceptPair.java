package ytex.kernel;

import java.io.Serializable;

/**
 * pair of concepts. used to submit a set of concepts to the similarity service
 * to compute pairwise similarity.
 * 
 * @author vijay
 * 
 */
public class ConceptPair implements Serializable, Comparable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String concept1;
	String concept2;

	public String getConcept1() {
		return concept1;
	}

	public void setConcept1(String concept1) {
		this.concept1 = concept1;
	}

	public String getConcept2() {
		return concept2;
	}

	public void setConcept2(String concept2) {
		this.concept2 = concept2;
	}

	public ConceptPair(String concept1, String concept2) {
		super();
		this.concept1 = concept1;
		this.concept2 = concept2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((concept1 == null) ? 0 : concept1.hashCode());
		result = prime * result
				+ ((concept2 == null) ? 0 : concept2.hashCode());
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
		ConceptPair other = (ConceptPair) obj;
		if (concept1 == null) {
			if (other.concept1 != null)
				return false;
		} else if (!concept1.equals(other.concept1))
			return false;
		if (concept2 == null) {
			if (other.concept2 != null)
				return false;
		} else if (!concept2.equals(other.concept2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConceptPair [concept1=" + concept1 + ", concept2=" + concept2
				+ "]";
	}

	public ConceptPair() {
		super();
	}

	/**
	 * compare concept 1, then concept 2
	 */
	@Override
	public int compareTo(Object o) {
		ConceptPair other = (ConceptPair)o;
		int c1 = getConcept1().compareTo(other.getConcept1());
		if(c1 != 0)
			return c1;
		return getConcept2().compareTo(other.getConcept2());
	}

}
