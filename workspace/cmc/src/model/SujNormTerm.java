package model;

import java.io.Serializable;

public class SujNormTerm implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int getNorm_term_id() {
		return norm_term_id;
	}
	public void setNorm_term_id(int norm_term_id) {
		this.norm_term_id = norm_term_id;
	}
	public int getLexical_unit_id() {
		return lexical_unit_id;
	}
	public void setLexical_unit_id(int lexical_unit_id) {
		this.lexical_unit_id = lexical_unit_id;
	}
	public String getNormTerm() {
		return normTerm;
	}
	public void setNormTerm(String normTerm) {
		this.normTerm = normTerm;
	}
	int norm_term_id;
	int lexical_unit_id;
	int num_concepts;
	public int getNum_concepts() {
		return num_concepts;
	}
	public void setNum_concepts(int num_concepts) {
		this.num_concepts = num_concepts;
	}
	String normTerm;
}
