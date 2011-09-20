package ytex.uima.mapper;

import java.util.Date;

import org.apache.uima.jcas.tcas.Annotation;

import ytex.model.Document;
import ytex.vacs.model.DocumentDateAnnotation;
import ytex.vacs.uima.types.DocumentDate;

public class DocumentDateAnnotationMapper extends
		AbstractDocumentAnnotationMapper<DocumentDateAnnotation, DocumentDate> {

	DocumentDateAnnotationMapper() {
		super(DocumentDateAnnotation.class, DocumentDate.class);
	}

	@Override
	public void mapAnnotationProperties(DocumentDateAnnotation anno,
			Annotation uimaAnno, Document doc) {
		super.mapAnnotationProperties(anno, uimaAnno, doc);
		DocumentDate docDate = (DocumentDate)uimaAnno;
		if (docDate.getDate() > 0) {
			anno.setDocumentDate(new Date(docDate.getDate()));
		}
	}

}
