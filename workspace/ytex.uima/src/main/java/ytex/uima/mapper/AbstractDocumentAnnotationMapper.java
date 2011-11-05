package ytex.uima.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.jcas.tcas.Annotation;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanUtils;

import ytex.model.Document;
import ytex.model.DocumentAnnotation;
import ytex.model.UimaType;

/**
 * Base annotation mapper class. This is *not* a spring managed object - we
 * instantiate them on demand. We do this because if we try to access an uima
 * annotation that is not in the current type system, we get an error.
 * 
 * @author vijay
 * 
 * @param <D>
 *            persistent document annotation class
 * @param <T>
 *            uima annotation class.
 */
public class AbstractDocumentAnnotationMapper<D extends DocumentAnnotation, T extends Annotation>
		implements DocumentAnnotationMapper<D> {
	private static final Log log = LogFactory
			.getLog(AbstractDocumentAnnotationMapper.class);
	Class<D> classDocumentAnnotation;
	Class<T> classUIMAAnnotation;

	public AbstractDocumentAnnotationMapper(Class<D> classDocumentAnnotation,
			Class<T> classUIMAAnnotation) {
		this.classDocumentAnnotation = classDocumentAnnotation;
		this.classUIMAAnnotation = classUIMAAnnotation;
	}

	/**
	 * Create the db DocumentAnnotation, attach it to the db document, and
	 * populate annotation fields. Calls mapAnnotationProperties to copy
	 * properties from uima annotation to db annotation.
	 * 
	 * @param annotation
	 *            uima annotation
	 * @param doc
	 *            document
	 * @param sessionFactory
	 *            hibernate session factory for persisting
	 * @return db DocumentAnnotation
	 */
	public D mapAnnotation(Annotation annotation, Document doc,
			SessionFactory sessionFactory) {
		D anno = createAnnotation(annotation, doc, sessionFactory);
		if (anno != null) {
			this.mapAnnotationProperties(anno, annotation, doc);
			sessionFactory.getCurrentSession().save(anno);
		}
		return anno;
	}

	protected D createAnnotation(Annotation annotation, Document doc,
			SessionFactory sessionFactory) {
		try {
			Constructor<D> ctor = classDocumentAnnotation.getConstructor(
					UimaType.class, Document.class);
			UimaType uimaType = getUimaTypeByName(annotation.getClass()
					.getName(), sessionFactory);
			D anno = ctor.newInstance(uimaType, doc);
			doc.getDocumentAnnotations().add(anno);
			return anno;
		} catch (NoSuchMethodException e) {
			log.error("error mapping annotation", e);
		} catch (InvocationTargetException e) {
			log.error("error mapping annotation", e);
		} catch (IllegalArgumentException e) {
			log.error("error mapping annotation", e);
		} catch (InstantiationException e) {
			log.error("error mapping annotation", e);
		} catch (IllegalAccessException e) {
			log.error("error mapping annotation", e);
		}
		return null;
	}

	/**
	 * Default implementation for mapping uima annotation to db annotation:
	 * simply copy whatever properties have the same name.
	 * 
	 * @param anno
	 * @param uimaAnno
	 * @param doc
	 */
	protected void mapAnnotationProperties(D anno, Annotation uimaAnno,
			Document doc) {
		BeanUtils.copyProperties(uimaAnno, anno);
		storeCoveredText(uimaAnno, anno);
	}

	/**
	 * Get uima type id from database (not the uima type id from the type system
	 * registry which is not fixed)
	 * 
	 * @param strName
	 * @param sessionFactory
	 * @return
	 */
	protected UimaType getUimaTypeByName(String strName,
			SessionFactory sessionFactory) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(
				"getUimaTypeByName");
		q.setCacheable(true);
		q.setString("uimaTypeName", strName);
		return (UimaType) q.uniqueResult();
	}

	protected void storeCoveredText(Annotation uimaAnno, D anno) {
		MapperConfig mc = MapperConfig.getConfig();
		if (mc != null && mc.getTypesStoreCoveredText().contains(
				classUIMAAnnotation.getName())) {
			String coveredText = uimaAnno.getCoveredText();
			if (coveredText.length() > mc.getCoveredTextMaxLen()) {
				coveredText = coveredText.substring(0,
						mc.getCoveredTextMaxLen() - 1);
			}
			anno.setCoveredText(coveredText);
		} else {
			anno.setCoveredText(null);
		}
	}
}
