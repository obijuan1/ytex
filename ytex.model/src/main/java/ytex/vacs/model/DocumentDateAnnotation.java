package ytex.vacs.model;

import java.util.Date;

import ytex.model.Document;
import ytex.model.DocumentAnnotation;
import ytex.model.UimaType;

/**
 * Mapped to vacs DocumentDateAnnotation.
 * @author vijay
 *
 */
public class DocumentDateAnnotation extends DocumentAnnotation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Date documentDate;

	public Date getDocumentDate() {
		return documentDate;
	}

	public void setDocumentDate(Date documentDate) {
		this.documentDate = documentDate;
	}

	@Override
	public String toString() {
		return "DocumentDateAnnotation [documentDate=" + documentDate
				+ ", toString()=" + super.toString() + "]";
	}

	public DocumentDateAnnotation() {
		super();
	}

	// public DocumentDateAnnotation(DocumentDate annotation, UimaType uimaType,
	// Document doc) {
	// super(annotation, uimaType, doc);
	// if(annotation.getDate() > 0) {
	// this.documentDate = new Date(annotation.getDate());
	// }
	// }
	public DocumentDateAnnotation(UimaType uimaType,
			Document doc) {
		super(uimaType, doc);
	}
}
