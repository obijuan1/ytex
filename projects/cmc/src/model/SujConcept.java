package model;

import java.io.Serializable;

public class SujConcept implements Serializable {

	public int getConcept_id() {
		return concept_id;
	}
	public void setConcept_id(int concept_id) {
		this.concept_id = concept_id;
	}

	public String getCui() {
		return cui;
	}
	public void setCui(String cui) {
		this.cui = cui;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int concept_id;
	int norm_term_id;
	public int getNorm_term_id() {
		return norm_term_id;
	}
	public void setNorm_term_id(int norm_term_id) {
		this.norm_term_id = norm_term_id;
	}
	String cui;
}
