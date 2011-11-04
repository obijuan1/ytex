package ytex.umls.model;

import java.io.Serializable;

public class UmlsAuiFirstWord implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String aui;

	String fstem;

	String fword;

	String stemmedStr;

	String tokenizedStr;
	
	public UmlsAuiFirstWord() {
		super();
	}
	public UmlsAuiFirstWord(String aui, String fword) {
		super();
		this.aui = aui;
		this.fword = fword;
	}
	public String getAui() {
		return aui;
	}
	public String getFstem() {
		return fstem;
	}
	public String getFword() {
		return fword;
	}
	public String getStemmedStr() {
		return stemmedStr;
	}

	public String getTokenizedStr() {
		return tokenizedStr;
	}

	public void setAui(String aui) {
		this.aui = aui;
	}

	public void setFstem(String fstem) {
		this.fstem = fstem;
	}

	public void setFword(String fword) {
		this.fword = fword;
	}

	public void setStemmedStr(String stemmedStr) {
		this.stemmedStr = stemmedStr;
	}

	public void setTokenizedStr(String tokenizedStr) {
		this.tokenizedStr = tokenizedStr;
	}

	@Override
	public String toString() {
		return "UmlsAuiFirstWord [aui=" + aui + ", fword=" + fword + "]";
	}

}
