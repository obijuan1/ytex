package ytex.uima.mapper;

import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.ae.type.Segment;
import ytex.model.Document;
import ytex.model.SegmentAnnotation;

public class SegmentAnnotationMapper extends
		AbstractDocumentAnnotationMapper<SegmentAnnotation, Segment> {

	SegmentAnnotationMapper() {
		super(SegmentAnnotation.class, Segment.class);
	}

	@Override
	public void mapAnnotationProperties(SegmentAnnotation anno,
			Annotation uimaAnno, Document doc) {
		super.mapAnnotationProperties(anno, uimaAnno, doc);
		anno.setSegmentID(((Segment) uimaAnno).getId());
	}

}
