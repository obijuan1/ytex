package ytex.uima.mapper;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.sql.DataSource;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.XMLSerializer;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.dao.DBUtil;
import ytex.model.Document;
import ytex.model.DocumentAnnotation;
import ytex.model.UimaType;
import ytex.uima.types.DocKey;
import ytex.uima.types.KeyValuePair;

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
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private CaseInsensitiveMap docTableCols = new CaseInsensitiveMap();
	private String formattedTableName = null;
	private static Set<Integer> stringTypes = new HashSet<Integer>();
	private static Set<Integer> numericTypes = new HashSet<Integer>();

	static {
		stringTypes.addAll(Arrays.asList(Types.CHAR, Types.NCHAR,
				Types.VARCHAR, Types.NVARCHAR));
		numericTypes.addAll(Arrays.asList(Types.BIGINT, Types.BIT,
				Types.BOOLEAN, Types.DECIMAL, Types.FLOAT, Types.DOUBLE,
				Types.INTEGER));
	}

	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

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
			Set<String> setTypesToIgnore, Set<String> typesStoreCoveredText,
			int coveredTextMaxLen) {
		try {
			//communicate options to mappers using thread local variable
			MapperConfig.setConfig(typesStoreCoveredText, coveredTextMaxLen);
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
			Query q = this.sessionFactory.getCurrentSession().getNamedQuery(
					"insertAnnotationContainmentLinks");
			q.setInteger("documentID", doc.getDocumentID());
			q.executeUpdate();

			return doc.getDocumentID();
		} finally {
			MapperConfig.unsetConfig();
		}
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
		if (annotation instanceof DocKey) {
			saveDocKey(document, (DocKey) annotation);
		} else if (setTypesToIgnore != null
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
	 * update the document table - set key values from dockey for the give
	 * document_id
	 * 
	 * @param document
	 *            document
	 * @param dk
	 *            key
	 */
	private void saveDocKey(Document document, DocKey dk) {
		int documentId = document.getDocumentID();
		FSArray fsa = dk.getKeyValuePairs();
		// build query dynamically
		StringBuilder queryBuilder = (new StringBuilder("update ")).append(
				formattedTableName).append(" set ");
		List<Object> args = new ArrayList<Object>();
		boolean bFirstArg = true;
		// iterate over key/value pairs
		for (int i = 0; i < fsa.size(); i++) {
			KeyValuePair kp = (KeyValuePair) fsa.get(i);
			String key = kp.getKey();
			if (key.equalsIgnoreCase("uid")) {
				// uid is something we 'know' about - set it
				document.setUid(kp.getValueLong());
			} else if (this.docTableCols.containsKey(key)) {
				// only attempt to map keys that correspond to valid columns
				boolean badArg = false;
				// verify that the value matches the datatype
				// if valueString not null then assume integer
				if (kp.getValueString() != null
						&& stringTypes.contains(docTableCols.get(key))) {
					args.add(kp.getValueString());
				} else if (numericTypes.contains(docTableCols.get(key))) {
					args.add(kp.getValueLong());
				} else {
					// invalid type for argument
					badArg = true;
					log.warn("document_id: " + documentId
							+ ", bad type for key=" + key + ", value="
							+ kp.getValueString() == null ? kp.getValueLong()
							: kp.getValueString());
				}
				if (!badArg) {
					// update
					if (!bFirstArg) {
						queryBuilder.append(", ");
					}
					queryBuilder.append(DBUtil.formatFieldName(key));
					queryBuilder.append("=? ");
				}
			}
		}
		if (args.size() > 0) {
			// make sure the document has been saved
			this.getSessionFactory().getCurrentSession().flush();
			// have something to update - add the where condition
			queryBuilder.append(" where document_id = ?");
			args.add(documentId);
			String sql = queryBuilder.toString();
			if (log.isDebugEnabled()) {
				log.debug(sql);
			}
			jdbcTemplate.update(sql, args.toArray());
		} else {
			log.warn("document_id: " + documentId + "could not map key");
		}
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

	public void initDocKeyMapping() {
		AbstractEntityPersister cm = (AbstractEntityPersister) this.sessionFactory
				.getClassMetadata(Document.class);
		// this.formattedTableName = DBUtil.formatTableName(cm.getTableName());
		this.formattedTableName = cm.getTableName();
		log.info("document table name = " + formattedTableName);
		final String query = "select * from " + formattedTableName
				+ " where 1=2";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			int nCols = rsmd.getColumnCount();
			for (int i = 1; i <= nCols; i++) {
				docTableCols.put(rsmd.getColumnName(i), rsmd.getColumnType(i));
			}
			if (log.isDebugEnabled()) {
				log.debug("docTableCols: " + docTableCols);
			}
		} catch (SQLException e) {
			log.error("problem determining document table fields", e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
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
				initDocKeyMapping();
				return null;
			}
		});
	}

}
