package model;

import java.io.Serializable;

public class SujLexicalUnit implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int lexical_unit_id;
	int document_id;
	public int getLexical_unit_id() {
		return lexical_unit_id;
	}
	public void setLexical_unit_id(int lexical_unit_id) {
		this.lexical_unit_id = lexical_unit_id;
	}
	public int getDocument_id() {
		return document_id;
	}
	public void setDocument_id(int document_id) {
		this.document_id = document_id;
	}
	public String getLexUnit() {
		return lexUnit;
	}
	public void setLexUnit(String lexUnit) {
		this.lexUnit = lexUnit;
	}
	String lexUnit;
}
