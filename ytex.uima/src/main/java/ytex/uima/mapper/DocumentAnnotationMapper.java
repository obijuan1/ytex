package ytex.uima.mapper;

import org.apache.uima.jcas.tcas.Annotation;
import org.hibernate.SessionFactory;

import ytex.model.Document;
import ytex.model.DocumentAnnotation;

public interface DocumentAnnotationMapper<D extends DocumentAnnotation> {

	/**
	 * @TODO
	 */
	public D mapAnnotation(Annotation annotation, Document doc,
			SessionFactory sessionFactory);
}