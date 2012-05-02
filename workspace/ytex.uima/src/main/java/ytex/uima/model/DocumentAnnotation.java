package ytex.uima.model;

import java.io.Serializable;

/**
 * Mapped to uima Annotation.
 * Base class for all annotations.
 * @author vijay
 *
 */
public class DocumentAnnotation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Integer begin;
	String coveredText;
	Document document;

	int documentAnnotationID;


	Integer end;
	UimaType uimaType;
	public DocumentAnnotation() {
		super();
	}
	public DocumentAnnotation(UimaType uimaType, Document doc) {
		this.uimaType = uimaType;
		this.document = doc;
	}

	public Integer getBegin() {
		return begin;
	}

	public String getCoveredText() {
		return coveredText;
	}

	public Document getDocument() {
		return document;
	}

	public int getDocumentAnnotationID() {
		return documentAnnotationID;
	}

	public Integer getEnd() {
		return end;
	}

	public UimaType getUimaType() {
		return uimaType;
	}

	public void setBegin(Integer begin) {
		this.begin = begin;
	}

	public void setCoveredText(String coveredText) {
		this.coveredText = coveredText;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setDocumentAnnotationID(int documentAnnotationID) {
		this.documentAnnotationID = documentAnnotationID;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}
	public void setUimaType(UimaType uimaType) {
		this.uimaType = uimaType;
	}
}
