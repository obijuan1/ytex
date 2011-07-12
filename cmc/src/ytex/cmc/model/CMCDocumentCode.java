package ytex.cmc.model;

import java.io.Serializable;

public class CMCDocumentCode implements Serializable {
	public CMCDocumentCode() {
		super();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CMCDocument document;
	private String code;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public CMCDocument getDocument() {
		return document;
	}
	public void setDocument(CMCDocument document) {
		this.document = document;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result
				+ ((document == null) ? 0 : document.hashCode());
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
		CMCDocumentCode other = (CMCDocumentCode) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (document == null) {
			if (other.document != null)
				return false;
		} else if (!document.equals(other.document))
			return false;
		return true;
	}
}
