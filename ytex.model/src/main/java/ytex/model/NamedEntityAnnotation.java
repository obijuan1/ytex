package ytex.model;

import java.util.ArrayList;
import java.util.List;

/**
 * mapped to cTAKES NamedEntityAnnotation
 * @author vijay
 *
 */
public class NamedEntityAnnotation extends DocumentAnnotation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Integer discoveryTechnique;
	Integer status;
	Integer certainty;
	Integer typeID;
	Float confidence;
	String segmentID;
	List<OntologyConceptAnnotation> ontologyConcepts = new ArrayList<OntologyConceptAnnotation>();
	public Integer getDiscoveryTechnique() {
		return discoveryTechnique;
	}
	public void setDiscoveryTechnique(Integer discoveryTechnique) {
		this.discoveryTechnique = discoveryTechnique;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getCertainty() {
		return certainty;
	}
	public void setCertainty(Integer certainty) {
		this.certainty = certainty;
	}
	public Integer getTypeID() {
		return typeID;
	}
	public void setTypeID(Integer typeID) {
		this.typeID = typeID;
	}
	public Float getConfidence() {
		return confidence;
	}
	public void setConfidence(Float confidence) {
		this.confidence = confidence;
	}
	public String getSegmentID() {
		return segmentID;
	}
	public void setSegmentID(String segmentID) {
		this.segmentID = segmentID;
	}
	public List<OntologyConceptAnnotation> getOntologyConcepts() {
		return ontologyConcepts;
	}
	public void setOntologyConcepts(List<OntologyConceptAnnotation> ontologyConcepts) {
		this.ontologyConcepts = ontologyConcepts;
	}
	@Override
	public String toString() {
		return "NamedEntityAnnotation [ontologyConcepts=" + ontologyConcepts
				+ ", toString()=" + super.toString() + "]";
	}
	public NamedEntityAnnotation() {
		super();
	}
	public NamedEntityAnnotation(UimaType uimaType, Document doc) {
		super(uimaType, doc);
	}
//	public NamedEntityAnnotation(NamedEntity annotation, UimaType uimaType, Document doc) {
//		super(annotation, uimaType, doc);
//		discoveryTechnique = annotation.getDiscoveryTechnique();
//		status = annotation.getStatus();
//		certainty = annotation.getCertainty();
//		typeID = annotation.getTypeID();
//		confidence = annotation.getConfidence();
//		segmentID = annotation.getSegmentID();
//	}
	
}
