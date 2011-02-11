package ytex.umls.model;

import java.io.Serializable;

/**
 * mapped to umls MRREL table
 */
public class MRREL implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String cui1;
	private String cui2;
	private String rel;
	private String sab;
	private String rela;

	public String getCui1() {
		return cui1;
	}

	public void setCui1(String cui1) {
		this.cui1 = cui1;
	}

	public String getCui2() {
		return cui2;
	}

	public void setCui2(String cui2) {
		this.cui2 = cui2;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getSab() {
		return sab;
	}

	public void setSab(String sab) {
		this.sab = sab;
	}

	public String getRela() {
		return rela;
	}

	public void setRela(String rela) {
		this.rela = rela;
	}
}
