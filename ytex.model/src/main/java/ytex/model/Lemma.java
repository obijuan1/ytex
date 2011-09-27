package ytex.model;

import java.io.Serializable;

/**
 * Mapped to Lemma.
 * @TODO map this
 * @author vijay
 *
 */
public class Lemma implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int lemmaId;
	private String key;
	private String posTag;
	public int getLemmaId() {
		return lemmaId;
	}
	public void setLemmaId(int lemmaId) {
		this.lemmaId = lemmaId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getPosTag() {
		return posTag;
	}
	public void setPosTag(String posTag) {
		this.posTag = posTag;
	}
	public Lemma() {
		super();
	}
}
