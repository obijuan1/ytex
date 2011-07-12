package ytex.model;

import java.io.Serializable;

/**
 * Mapped to cTAKES Segment
 * @author vijay
 *
 */
public class SegmentAnnotation extends DocumentAnnotation implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String segmentID;

	public String getSegmentID() {
		return segmentID;
	}

	public void setSegmentID(String segmentID) {
		this.segmentID = segmentID;
	}

	public SegmentAnnotation() {
		super();
	}

	public SegmentAnnotation(UimaType uimaType, Document doc) {
		super(uimaType, doc);
	}

//	public SegmentAnnotation(Segment annotation, UimaType uimaType,
//			Document doc) {
//		super(annotation, uimaType, doc);
//		this.segmentID = annotation.getId();
//	}
	

}
