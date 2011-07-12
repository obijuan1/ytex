package ytex.cmc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CMCDocument implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int documentId;
	String documentSet;
	List<CMCDocumentCode> documentCodes = new ArrayList<CMCDocumentCode>(1);
	String clinicalHistory;
	String impression;
	
	public List<CMCDocumentCode> getDocumentCodes() {
		return documentCodes;
	}
	public void setDocumentCodes(List<CMCDocumentCode> documentCodes) {
		this.documentCodes = documentCodes;
	}
	public String getClinicalHistory() {
		return clinicalHistory;
	}
	public void setClinicalHistory(String clinicalHistory) {
		this.clinicalHistory = clinicalHistory;
	}
	public String getImpression() {
		return impression;
	}
	public void setImpression(String impression) {
		this.impression = impression;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + documentId;
		result = prime * result
				+ ((documentSet == null) ? 0 : documentSet.hashCode());
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
		CMCDocument other = (CMCDocument) obj;
		if (documentId != other.documentId)
			return false;
		if (documentSet == null) {
			if (other.documentSet != null)
				return false;
		} else if (!documentSet.equals(other.documentSet))
			return false;
		return true;
	}
	public CMCDocument(int documentId, String documentSet) {
		super();
		this.documentId = documentId;
		this.documentSet = documentSet;
	}
	public CMCDocument() {
		super();
		// TODO Auto-generated constructor stub
	}
	public int getDocumentId() {
		return documentId;
	}
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}
	public String getDocumentSet() {
		return documentSet;
	}
	public void setDocumentSet(String documentSet) {
		this.documentSet = documentSet;
	}
	
	

}
