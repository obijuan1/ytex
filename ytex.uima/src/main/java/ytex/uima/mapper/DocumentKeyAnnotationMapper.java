package ytex.uima.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.jcas.tcas.Annotation;

import ytex.model.Document;
import ytex.vacs.model.DocumentKeyAnnotation;
import ytex.vacs.uima.types.DocumentKey;

public class DocumentKeyAnnotationMapper extends
		AbstractDocumentAnnotationMapper<DocumentKeyAnnotation, DocumentKey> {
	private static final Log log = LogFactory
			.getLog(DocumentKeyAnnotationMapper.class);

	DocumentKeyAnnotationMapper() {
		super(DocumentKeyAnnotation.class, DocumentKey.class);
	}

	@Override
	public void mapAnnotationProperties(DocumentKeyAnnotation anno,
			Annotation uimaAnno, Document doc) {
		DocumentKey docKey = (DocumentKey) uimaAnno;
		anno.setUid(docKey.getUid());
		anno.setSiteID(docKey.getSiteID());
		if(docKey.getStudyID() != 0)
			anno.setStudyID(docKey.getStudyID());
		if(docKey.getDocumentType() != 0)
			anno.setDocumentTypeID(docKey.getDocumentType());
	}

}
