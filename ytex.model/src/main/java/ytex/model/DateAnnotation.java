package ytex.model;

import java.util.Date;

import ytex.model.Document;
import ytex.model.DocumentAnnotation;
import ytex.model.UimaType;

/**
 * Mapped to vacs DocumentDateAnnotation.
 * @author vijay
 *
 */
public class DateAnnotation extends DocumentAnnotation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Date date;


	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "DateAnnotation [date=" + date
				+ ", toString()=" + super.toString() + "]";
	}

	public DateAnnotation() {
		super();
	}

	// public DocumentDateAnnotation(DocumentDate annotation, UimaType uimaType,
	// Document doc) {
	// super(annotation, uimaType, doc);
	// if(annotation.getDate() > 0) {
	// this.documentDate = new Date(annotation.getDate());
	// }
	// }
	public DateAnnotation(UimaType uimaType,
			Document doc) {
		super(uimaType, doc);
	}
}
