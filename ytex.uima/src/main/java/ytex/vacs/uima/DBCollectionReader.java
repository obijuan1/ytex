package ytex.vacs.uima;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import ytex.uima.ApplicationContextHolder;
import ytex.vacs.uima.types.DocumentKey;

/**
 * 
 * Read documents from db. Config parameters:
 * <ul>
 * <li>queryGetDocumentKeys the query to get the document keys</li>
 * <li>queryGetDocument the query to get a document given a key. should have
 * named parameters that match the columns of the result set returned by
 * queryGetDocumentKeys</li>
 * <li>keyTypeName the uima type of the document key to be added to the cas.
 * defaults to ytex.vacs.uima.types.DocumentKey.
 * </ul>
 * 
 * @TODO more doc
 * @author vijay
 * 
 */
public class DBCollectionReader extends CollectionReader_ImplBase {
	private static final Log log = LogFactory.getLog(DBCollectionReader.class);

	/**
	 * the query to get the document keys set in config file
	 */
	protected String queryGetDocumentKeys;
	/**
	 * the queyr to get a document given a key. set in config file
	 */
	protected String queryGetDocument;
	/**
	 * the key type. if not set, will default to
	 * ytex.vacs.uima.types.DocumentKey.
	 */
	protected String keyTypeName;

	protected DataSource dataSource;
	protected SimpleJdbcTemplate simpleJdbcTemplate;
	protected NamedParameterJdbcTemplate namedJdbcTemplate;
	List<Map<String, Object>> listDocumentIds;
	int i = 0;

	@Override
	public void initialize() throws ResourceInitializationException {
		initializePreLoad();
		loadDocumentIds();
	}

	protected void initializePreLoad() throws ResourceInitializationException {
		super.initialize();
		ProcessingResourceMetaData metaData = getProcessingResourceMetaData();
		ConfigurationParameterSettings paramSettings = metaData
				.getConfigurationParameterSettings();
		this.queryGetDocumentKeys = (String) paramSettings
				.getParameterValue("queryGetDocumentKeys");
		this.queryGetDocument = (String) paramSettings
				.getParameterValue("queryGetDocument");
		this.keyTypeName = (String) paramSettings
				.getParameterValue("keyTypeName");
		dataSource = (DataSource) ApplicationContextHolder
				.getApplicationContext().getBean("dataSource");
		simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
		namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	protected void loadDocumentIds() {
		if (listDocumentIds == null) {
			listDocumentIds = simpleJdbcTemplate
					.queryForList(queryGetDocumentKeys);
			i = 0;
		}
	}

	@Override
	public void getNext(final CAS aCAS) throws IOException, CollectionException {
		if (i < listDocumentIds.size()) {
			final Map<String, Object> id = listDocumentIds.get(i++);
			if (log.isInfoEnabled()) {
				log.info("loading document with id = " + id);
			}
			getDocumentById(aCAS, id);
			try {
				if (keyTypeName != null) {
					Annotation key = (Annotation)ConstructorUtils.invokeConstructor(Class
							.forName(keyTypeName), aCAS.getJCas());
					// just copy properties with the specified key names
					BeanUtils.populate(key, id);
					if (key instanceof DocumentKey) {
						// backwards compatibility
						DocumentKey docKey = (DocumentKey)key;
						if (id.get("studyid") != null)
							docKey.setStudyID((Integer) id.get("studyid"));
						if (id.get("uid") != null)
							docKey.setUid((Integer) id.get("uid"));
						if (id.get("document_type_id") != null)
							docKey.setDocumentType((Integer) id
									.get("document_type_id"));
						if (id.get("site_id") != null)
							docKey.setSiteID((String) id.get("site_id"));
					}
					key.addToIndexes();
				}
			} catch (CASException ce) {
				throw new CollectionException(ce);
			} catch (InvocationTargetException ce) {
				throw new CollectionException(ce);
			} catch (IllegalAccessException ce) {
				throw new CollectionException(ce);
			} catch (NoSuchMethodException ce) {
				throw new CollectionException(ce);
			} catch (InstantiationException ce) {
				throw new CollectionException(ce);
			} catch (ClassNotFoundException ce) {
				throw new CollectionException(ce);
			}
		} else {
			// shouldn't get here?
			throw new CollectionException("no documents to process",
					new Object[] {});
		}
	}

	protected void getDocumentById(final CAS aCAS, final Map<String, Object> id) {
		namedJdbcTemplate.query(queryGetDocument, id,
				new RowCallbackHandler() {
					boolean bFirstRowRead = false;

					@Override
					public void processRow(ResultSet rs)
							throws SQLException {
						if (!bFirstRowRead) {
							LobHandler lobHandler = new DefaultLobHandler();
							String clobText = lobHandler.getClobAsString(
									rs, 1);
							aCAS.setDocumentText(clobText);
						} else {
							log
									.error("Multiple documents for document key: "
											+ id);
						}
					}
				});
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(i, listDocumentIds.size(),
				Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return i < listDocumentIds.size();
	}

	@Override
	public void close() throws IOException {
		this.listDocumentIds = null;
		this.i = 0;
	}

}
