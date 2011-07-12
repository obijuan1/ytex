package ytex.model;

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
	int documentAnnotationID;
	Document document;
	public UimaType getUimaType() {
		return uimaType;
	}

	public void setUimaType(UimaType uimaType) {
		this.uimaType = uimaType;
	}


	Integer begin;
	Integer end;
	UimaType uimaType;

	public DocumentAnnotation() {
		super();
	}

	public int getDocumentAnnotationID() {
		return documentAnnotationID;
	}

	public void setDocumentAnnotationID(int documentAnnotationID) {
		this.documentAnnotationID = documentAnnotationID;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Integer getBegin() {
		return begin;
	}

	public void setBegin(Integer begin) {
		this.begin = begin;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}
	public DocumentAnnotation(UimaType uimaType, Document doc) {
		this.uimaType = uimaType;
		this.document = doc;
	}

//	public DocumentAnnotation(Annotation annotation, UimaType uimaType, Document doc) {
//		this.uimaType = uimaType;
//		this.document = doc;
//		this.begin = annotation.getBegin();
//		this.end = annotation.getEnd();
//	}
}
