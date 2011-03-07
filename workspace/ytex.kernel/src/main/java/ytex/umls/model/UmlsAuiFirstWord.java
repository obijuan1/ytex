package ytex.umls.model;

import java.io.Serializable;

public class UmlsAuiFirstWord implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getAui() {
		return aui;
	}

	public void setAui(String aui) {
		this.aui = aui;
	}

	public String getFword() {
		return fword;
	}

	public void setFword(String fword) {
		this.fword = fword;
	}

	String aui;
	String fword;

	public UmlsAuiFirstWord() {
		super();
	}

	public UmlsAuiFirstWord(String aui, String fword) {
		super();
		this.aui = aui;
		this.fword = fword;
	}

	@Override
	public String toString() {
		return "UmlsAuiFirstWord [aui=" + aui + ", fword=" + fword + "]";
	}

}
