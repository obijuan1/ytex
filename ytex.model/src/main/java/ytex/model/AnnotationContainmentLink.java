package ytex.model;

import java.io.Serializable;

/**
 * represent a containment relationship between annotations, e.g. sentences
 * contain words.
 * 
 * @author vijay
 * 
 */
public class AnnotationContainmentLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	int annotationContainmentLinkId;
	int parentAnnotationId;
	int parentUimaTypeId;
	int childAnnotationId;
	int childUimaTypeId;
	
	
//	public int getAnnotationContainmentLinkId() {
//		return annotationContainmentLinkId;
//	}
//	public void setAnnotationContainmentLinkId(int annotationContainmentLinkId) {
//		this.annotationContainmentLinkId = annotationContainmentLinkId;
//	}
	public int getParentAnnotationId() {
		return parentAnnotationId;
	}
	public void setParentAnnotationId(int parentAnnotationId) {
		this.parentAnnotationId = parentAnnotationId;
	}
	public int getParentUimaTypeId() {
		return parentUimaTypeId;
	}
	public void setParentUimaTypeId(int parentUimaTypeId) {
		this.parentUimaTypeId = parentUimaTypeId;
	}
	public int getChildAnnotationId() {
		return childAnnotationId;
	}
	public void setChildAnnotationId(int childAnnotationId) {
		this.childAnnotationId = childAnnotationId;
	}
	public int getChildUimaTypeId() {
		return childUimaTypeId;
	}
	public void setChildUimaTypeId(int childUimaTypeId) {
		this.childUimaTypeId = childUimaTypeId;
	}
	
}
