package ytex.uima.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.jcas.tcas.Annotation;
import org.hibernate.SessionFactory;

import ytex.model.Document;
import ytex.vacs.model.DocumentKeyAnnotation;
import ytex.vacs.uima.types.DocumentKey;

/*
 * Save the uid in the document's uid field, if the uid is defined.
 */
public class DocumentKeyAnnotationMapper extends
		AbstractDocumentAnnotationMapper<DocumentKeyAnnotation, DocumentKey> {
	private static final Log log = LogFactory
			.getLog(DocumentKeyAnnotationMapper.class);

	DocumentKeyAnnotationMapper() {
		super(DocumentKeyAnnotation.class, DocumentKey.class);
	}

	@Override
	public DocumentKeyAnnotation mapAnnotation(Annotation annotation,
			Document doc, SessionFactory sessionFactory) {
		DocumentKey docKey = (DocumentKey) annotation;
		if (docKey.getUid() != 0)
			doc.setUid(docKey.getUid());
		return super.mapAnnotation(annotation, doc, sessionFactory);
	}

	@Override
	public void mapAnnotationProperties(DocumentKeyAnnotation anno,
			Annotation uimaAnno, Document doc) {
		DocumentKey docKey = (DocumentKey) uimaAnno;
		anno.setUid(docKey.getUid());
		anno.setSiteID(docKey.getSiteID());
		if (docKey.getStudyID() != 0)
			anno.setStudyID(docKey.getStudyID());
		if (docKey.getDocumentType() != 0)
			anno.setDocumentTypeID(docKey.getDocumentType());
	}

}
