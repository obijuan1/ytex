package ytex.uima.mapper;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.XMLSerializer;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.model.Document;
import ytex.model.DocumentAnnotation;
import ytex.model.UimaType;

/**
 * Map document annotations to the database. Delegates to AnnotationMapper
 * implementations. AnnotationMappers are configured in the database
 * (REF_UIMA_TYPE).
 * 
 * @author vijay
 * 
 */
public class DocumentMapperServiceImpl implements DocumentMapperService,
		InitializingBean {
	private static final Log log = LogFactory
			.getLog(DocumentMapperServiceImpl.class);
	private SessionFactory sessionFactory;
	private PlatformTransactionManager transactionManager;

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * date format for analysis batch.
	 */
	private static final ThreadLocal<DateFormat> tlAnalysisBatchDateFormat = new ThreadLocal<DateFormat>() {
		public DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm");
		}
	};
	/**
	 * map of uima annotation class name to mapper class name
	 */
	private Map<String, String> documentAnnotationMappers;
	/**
	 * thread local cache of mappers. instantiate them on-demand so that we
	 * don't run into trouble with trying to access Uima Annotations that are
	 * not in the type system.
	 */
	private static final ThreadLocal<Map<String, DocumentAnnotationMapper<? extends DocumentAnnotation>>> mappers = new ThreadLocal<Map<String, DocumentAnnotationMapper<? extends DocumentAnnotation>>>() {
		@Override
		protected Map<String, DocumentAnnotationMapper<? extends DocumentAnnotation>> initialValue() {
			return new HashMap<String, DocumentAnnotationMapper<? extends DocumentAnnotation>>();
		}
	};

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private String getDefaultAnalysisBatch() {
		return tlAnalysisBatchDateFormat.get().format(new Date());
	}

	private Document createDocument(JCas jcas, String analysisBatch,
			boolean bStoreDocText, boolean bStoreCAS) {
		Document doc = new Document();
		if (bStoreDocText)
			doc.setDocText(jcas.getDocumentText());
		doc.setAnalysisBatch(analysisBatch == null
				|| analysisBatch.length() == 0 ? getDefaultAnalysisBatch()
				: analysisBatch);
		if (bStoreCAS) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream zipOut = new GZIPOutputStream(out);
				XmiCasSerializer ser = new XmiCasSerializer(
						jcas.getTypeSystem());
				XMLSerializer xmlSer = new XMLSerializer(zipOut, false);
				ser.serialize(jcas.getCas(), xmlSer.getContentHandler());
				zipOut.close();
				doc.setCas(out.toByteArray());
			} catch (Exception saxException) {
				log.error("error serializing document cas", saxException);
			}
		}
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.dao.mapper.DocumentMapperService#saveDocument(org.apache.uima.jcas
	 * .JCas, java.lang.String)
	 */
	public Integer saveDocument(JCas jcas, String analysisBatch,
			boolean bStoreDocText, boolean bStoreCAS,
			Set<String> setTypesToIgnore) {
		Document doc = createDocument(jcas, analysisBatch, bStoreDocText,
				bStoreCAS);
		this.sessionFactory.getCurrentSession().save(doc);
		AnnotationIndex annoIdx = jcas
				.getAnnotationIndex(Annotation.typeIndexID);
		FSIterator annoIterator = annoIdx.iterator();
		while (annoIterator.hasNext()) {
			saveDocumentAnnotation((Annotation) annoIterator.next(), doc,
					setTypesToIgnore);
		}
		// this causes deadlocks when running in parallel!
		// Query q = this.sessionFactory.getCurrentSession().getNamedQuery(
		// "insertAnnotationContainmentLinks");
		// q.setInteger("documentID", doc.getDocumentID());
		// q.executeUpdate();
		return doc.getDocumentID();
	}

	/**
	 * save an annotation.
	 * 
	 * @param annotation
	 *            uima annotation
	 * @param document
	 *            document to which it belongs
	 * @return null if the annotation is not mapped
	 */
	private DocumentAnnotation saveDocumentAnnotation(Annotation annotation,
			Document document, Set<String> setTypesToIgnore) {
		if (setTypesToIgnore != null
				&& !setTypesToIgnore.contains(annotation.getClass().getName())) {
			DocumentAnnotationMapper<? extends DocumentAnnotation> mapper = this
					.getMapperForAnnotation(annotation.getClass().getName());
			if (mapper != null) {
				DocumentAnnotation docAnno = (DocumentAnnotation) mapper
						.mapAnnotation(annotation, document,
								this.getSessionFactory());
				return docAnno;
			}
		}
		return null;
	}

	/**
	 * instantiate/get mapper for given uima annotation
	 * 
	 * @param uimaClassName
	 *            uima annotation
	 * @return mapper or null if not mapped
	 */
	@SuppressWarnings("unchecked")
	private DocumentAnnotationMapper<? extends DocumentAnnotation> getMapperForAnnotation(
			String uimaClassName) {
		DocumentAnnotationMapper<? extends DocumentAnnotation> mapper = null;
		String mapperClassName = this.documentAnnotationMappers
				.get(uimaClassName);
		if (mapperClassName != null) {
			mapper = mappers.get().get(mapperClassName);
			if (mapper == null) {
				try {
					mapper = (DocumentAnnotationMapper<? extends DocumentAnnotation>) Class
							.forName(mapperClassName).newInstance();
					mappers.get().put(mapperClassName, mapper);
				} catch (Exception e) {
					log.error("Error instantiating mapper: " + mapperClassName,
							e);
					throw new RuntimeException(e);
				}
			}
		}
		return mapper;

	}

	/**
	 * load the map of uima annotation class name to mapper class name from the
	 * database.
	 * 
	 * For some reason this is not getting executed within a transaction.
	 * Manually wrap the db access in a transaction.
	 * 
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() {
		documentAnnotationMappers = new HashMap<String, String>();
		TransactionTemplate txTemplate = new TransactionTemplate(
				this.getTransactionManager());
		txTemplate
				.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
		txTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				Query q = getSessionFactory().getCurrentSession()
						.getNamedQuery("getUimaTypes");
				List<UimaType> uimaTypes = q.list();
				for (UimaType uimaType : uimaTypes) {
					documentAnnotationMappers.put(uimaType.getUimaTypeName(),
							uimaType.getMapperName());
				}
				return null;
			}
		});
	}

}
