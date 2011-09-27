package ytex.uima.mapper;

import org.apache.uima.jcas.tcas.Annotation;

import ytex.model.DocumentAnnotation;

public class AnnotationMapper extends
		AbstractDocumentAnnotationMapper<DocumentAnnotation, Annotation> {

	AnnotationMapper() {
		super(DocumentAnnotation.class, Annotation.class);
	}

}
