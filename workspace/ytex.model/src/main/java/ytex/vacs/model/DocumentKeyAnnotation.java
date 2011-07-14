package ytex.vacs.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.model.Document;
import ytex.model.DocumentAnnotation;
import ytex.model.UimaType;

/**
 * Mapped to vacs DocumentKeyAnnotation
 * @author vijay
 *
 */
public class DocumentKeyAnnotation extends DocumentAnnotation {
	private static final Log log = LogFactory.getLog(DocumentKeyAnnotation.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Long uid;
	Integer studyID;
	Integer documentTypeID;
	String siteID;

	public String getSiteID() {
		return siteID;
	}

	public void setSiteID(String siteID) {
		this.siteID = siteID;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public Integer getStudyID() {
		return studyID;
	}

	public void setStudyID(Integer studyID) {
		this.studyID = studyID;
	}

	public Integer getDocumentTypeID() {
		return documentTypeID;
	}

	public void setDocumentTypeID(Integer documentTypeID) {
		this.documentTypeID = documentTypeID;
	}	
	
	public DocumentKeyAnnotation() {
	}

	// public DocumentKeyAnnotation(DocumentKey docKey, UimaType uimaType,
	// Document doc) {
	// super(docKey, uimaType, doc);
	public DocumentKeyAnnotation(UimaType uimaType, Document doc) {
		super(uimaType, doc);
//		this.setStudyID(docKey.getStudyID());
//		this.setUid(docKey.getUid());
//		int docTypeId = docKey.getDocumentType();
//		if (docTypeId < DocumentType.values().length) {
//			this.setDocumentType(DocumentType.values()[docTypeId]);
//		} else {
//			log.warn("invalid doc type, using progress note as document type.");
//			this.setDocumentType(DocumentType.PROGRESS_NOTE);
//		}
//		this.setSiteID(docKey.getSiteID() != null ? docKey.getSiteID() : "");
	}

}
