package ytex.uima.mapper;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.sql.DataSource;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.XMLSerializer;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

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
	/**
	 * holder for FeatureStruct attributes
	 * 
	 * @author vijay
	 * 
	 */
	public static class AnnoFSAttribute {
		private int annoBaseId;

		private FeatureStructure fs;

		private Integer index;

		public AnnoFSAttribute() {
			super();
		}

		public AnnoFSAttribute(int annoBaseId, FeatureStructure fs,
				Integer index) {
			super();
			this.annoBaseId = annoBaseId;
			this.fs = fs;
			this.index = index;
		}

		public int getAnnoBaseId() {
			return annoBaseId;
		}

		public FeatureStructure getFs() {
			return fs;
		}

		public Integer getIndex() {
			return index;
		}

		public void setAnnoBaseId(int annoBaseId) {
			this.annoBaseId = annoBaseId;
		}

		public void setFs(FeatureStructure fs) {
			this.fs = fs;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}

	}

	private static final Log log = LogFactory
			.getLog(DocumentMapperServiceImpl.class);
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
	private static Set<Integer> numericTypes = new HashSet<Integer>();
	private static Set<Integer> stringTypes = new HashSet<Integer>();
	/**
	 * date format for analysis batch.
	 */
	private static final ThreadLocal<DateFormat> tlAnalysisBatchDateFormat = new ThreadLocal<DateFormat>() {
		public DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm");
		}
	};
	static {
		stringTypes.addAll(Arrays.asList(Types.CHAR, Types.NCHAR,
				Types.VARCHAR, Types.NVARCHAR));
		numericTypes.addAll(Arrays.asList(Types.BIGINT, Types.BIT,
				Types.BOOLEAN, Types.DECIMAL, Types.FLOAT, Types.DOUBLE,
				Types.INTEGER));
	}
	private DataSource dataSource;
	private String dbSchema;
	private String dbType;
	private Dialect dialect;
	private String dialectClassName;
	private CaseInsensitiveMap docTableCols = new CaseInsensitiveMap();

	private String formattedTableName = null;

	private JdbcTemplate jdbcTemplate;

	private Map<String, AnnoMappingInfo> mapAnnoMappingInfo = new HashMap<String, AnnoMappingInfo>();
	private SessionFactory sessionFactory;
	private ThreadLocal<Map<String, AnnoMappingInfo>> tl_mapAnnoMappingInfo = new ThreadLocal<Map<String, AnnoMappingInfo>>() {

		@Override
		protected Map<String, AnnoMappingInfo> initialValue() {
			return new HashMap<String, AnnoMappingInfo>();
		}

	};
	/**
	 * map of annotation to fields that need to be mapped
	 */
	private ThreadLocal<SetMultimap<String, String>> tl_mapFieldInfo = new ThreadLocal<SetMultimap<String, String>>() {
		@Override
		protected SetMultimap<String, String> initialValue() {
			return HashMultimap.create();
		}

	};

	private PlatformTransactionManager transactionManager;

	private Map<String, UimaType> uimaTypeMap = new HashMap<String, UimaType>();

	private Properties ytexProperties;

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
					uimaTypeMap.put(uimaType.getUimaTypeName(), uimaType);
				}
				initDocKeyMapping();
				return null;
			}
		});
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

	private void extractAndSaveDocKey(JCas jcas, Document doc) {
		AnnotationIndex<Annotation> idx = jcas
				.getAnnotationIndex(DocKey.typeIndexID);
		FSIterator<Annotation> annoIterator = idx.iterator();
		if (annoIterator.hasNext())
			this.saveDocKey(doc, (DocKey) annoIterator.next());
	}

	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	public String getDbSchema() {
		return dbSchema;
	}

	public String getDbType() {
		return dbType;
	}

	private String getDefaultAnalysisBatch() {
		return tlAnalysisBatchDateFormat.get().format(new Date());
	}

	public String getDialectClassName() {
		return dialectClassName;
	}

	public Map<String, AnnoMappingInfo> getMapAnnoMappingInfo() {
		return mapAnnoMappingInfo;
	}

	private AnnoMappingInfo getMapInfo(Type type) {
		String className = type.getName();
		// if the key is there, then return it (may be null)
		AnnoMappingInfo mapInfo = null;
		if (this.tl_mapAnnoMappingInfo.get().containsKey(className)) {
			mapInfo = this.tl_mapAnnoMappingInfo.get().get(className);
		} else {
			// load the mappinginfo, save in cache
			mapInfo = initMapInfo(type);
			this.tl_mapAnnoMappingInfo.get().put(className, mapInfo);
		}
		return mapInfo;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public Properties getYtexProperties() {
		return ytexProperties;
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
	 * load mapping info
	 * 
	 * @param type
	 * @return
	 */
	private AnnoMappingInfo initMapInfo(Type type) {
		String annoName = type.getShortName().toLowerCase();
		AnnoMappingInfo mapInfo;
		if (this.mapAnnoMappingInfo.containsKey(type.getName())) {
			mapInfo = this.mapAnnoMappingInfo.get(type.getName()).deepCopy();
		} else {
			mapInfo = new AnnoMappingInfo();
		}
		if (Strings.isNullOrEmpty(mapInfo.getTableName()))
			mapInfo.setTableName("anno_" + annoName);
		List<Feature> features = type.getFeatures();
		// get the non primitive fields
		for (Feature f : features) {
			if (f.getRange().isArray()
					&& !f.getRange().getComponentType().isPrimitive()) {
				// add this field to the list of fields to store
				this.tl_mapFieldInfo.get()
						.put(type.getName(), f.getShortName());
			}
		}
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			DatabaseMetaData dmd = conn.getMetaData();
			// get columns for corresponding table
			rs = dmd.getColumns(null, null, mapInfo.getTableName(), null);
			while (rs.next()) {
				String colName = rs.getString("COLUMN_NAME");
				int colSize = rs.getInt("COLUMN_SIZE");
				if (!"anno_base_id".equals(colName)) {
					// possibility 1: the column is already mapped to the field
					// if so, then just set the size
					if (!updateSize(mapInfo, colName, colSize)) {
						// // possibility 2: the column is not mapped - see if
						// it
						// // matches a field
						// for (Feature f : features) {
						// String annoFieldName = f.getShortName();
						// if (annoFieldName.equalsIgnoreCase(colName)) {
						// // create new field mapping info
						// FieldMappingInfo fmap = new FieldMappingInfo();
						// fmap.setAnnoFieldName(annoFieldName);
						// fmap.setSize(colSize);
						// fmap.setTableFieldName(colName);
						// mapInfo.getMapField().put(annoFieldName, fmap);
						// }
						// }
						// iterate through features, see which match the column
						for (Feature f : features) {
							String annoFieldName = f.getShortName();
							if (f.getRange().isPrimitive()
									&& annoFieldName.equalsIgnoreCase(colName)
									&& !mapInfo.getMapField().containsKey(
											annoFieldName)) {
								FieldMappingInfo fmap = new FieldMappingInfo();
								fmap.setAnnoFieldName(annoFieldName);
								fmap.setTableFieldName(colName);
								fmap.setSize(colSize);
								mapInfo.getMapField().put(annoFieldName, fmap);
								break;
							}
						}
					}
				}
			}
			// don't map this annotation if no fields match columns
			if (mapInfo.getMapField().size() == 0)
				mapInfo = null;
			else {
				// generate sql
				String tablePrefix = "";
				if ("mssql".equals(dbType)) {
					tablePrefix = dbSchema + ".";
				}
				StringBuilder b = new StringBuilder("insert into ");
				b.append(tablePrefix).append(mapInfo.getTableName());
				b.append("(anno_base_id");
				for (Map.Entry<String, FieldMappingInfo> fieldEntry : mapInfo
						.getMapField().entrySet()) {
					b.append(", ").append(dialect.openQuote())
							.append(fieldEntry.getValue().getTableFieldName())
							.append(dialect.closeQuote());
				}
				b.append(") values (?")
						.append(Strings.repeat(",?", mapInfo.getMapField()
								.size())).append(")");
				mapInfo.setSql(b.toString());
				if (log.isInfoEnabled())
					log.info("sql insert for type " + type.getName() + ": "
							+ mapInfo.getSql());
			}
			if (log.isDebugEnabled())
				log.debug("initMapInfo(" + annoName + "): " + mapInfo);
			return mapInfo;
		} catch (SQLException sqe) {
			throw new RuntimeException(sqe);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * update column size for given column, if the column has been mapped
	 * 
	 * @param mapInfo
	 * @param colName
	 * @param colSize
	 * @return true column is mapped to a field
	 */
	private boolean updateSize(AnnoMappingInfo mapInfo, String colName,
			int colSize) {
		for (FieldMappingInfo fi : mapInfo.getMapField().values()) {
			if (colName.equalsIgnoreCase(fi.getTableFieldName())) {
				if (fi.getSize() <= 0)
					fi.setSize(colSize);
				return true;
			}
		}
		return false;
	}

	private BiMap<TOP, Integer> saveAnnoBase(JCas jcas,
			Set<String> setTypesToIgnore, Set<String> typesStoreCoveredText,
			int coveredTextMaxLen, Document doc) {
		AnnotationIndex<Annotation> annoIdx = jcas
				.getAnnotationIndex(Annotation.typeIndexID);
		List<TOP> listAnno = new ArrayList<TOP>(annoIdx.size());
		BiMap<TOP, Integer> mapAnnoToId = HashBiMap.create();
		FSIterator<Annotation> annoIterator = annoIdx.iterator();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int docId = doc.getDocumentID();
		try {
			conn = this.getDataSource().getConnection();
			ps = conn
					.prepareStatement(
							"insert into anno_base (document_id, span_begin, span_end, uima_type_id, covered_text) values (?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
			while (annoIterator.hasNext()) {
				Annotation anno = (Annotation) annoIterator.next();
				String annoClass = anno.getClass().getName();
				if (!setTypesToIgnore.contains(annoClass)
						&& this.uimaTypeMap.containsKey(annoClass)) {
					// should not ignore, and we know how to map this annotation
					listAnno.add(anno);
					ps.setInt(1, docId);
					ps.setInt(2, anno.getBegin());
					ps.setInt(3, anno.getEnd());
					ps.setInt(4, this.uimaTypeMap.get(annoClass)
							.getUimaTypeID());
					if (typesStoreCoveredText.contains(annoClass)) {
						String coveredText = anno.getCoveredText();
						if (coveredText.length() > coveredTextMaxLen) {
							coveredText = coveredText.substring(0,
									coveredTextMaxLen - 1);
						}
						ps.setString(5, coveredText);
					} else {
						ps.setNull(5, Types.VARCHAR);
					}
					ps.addBatch();
				}
			}
			ps.executeBatch();
			rs = ps.getGeneratedKeys();
			int annoIndex = 0;
			while (rs.next()) {
				mapAnnoToId.put(listAnno.get(annoIndex), rs.getInt(1));
				annoIndex++;
			}
			return mapAnnoToId;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	protected void saveAnnoBindVariables(final Type type,
			final AnnoMappingInfo mapInfo, PreparedStatement ps, int annoId,
			FeatureStructure anno) throws SQLException {
		// set anno_base_id
		ps.setInt(1, annoId);
		int argIdx = 2;
		// iterate over fields
		for (Map.Entry<String, FieldMappingInfo> fieldEntry : mapInfo
				.getMapField().entrySet()) {
			String fieldName = fieldEntry.getKey();
			FieldMappingInfo fieldMapInfo = fieldEntry.getValue();
			Feature feat = type.getFeatureByBaseName(fieldName);
			if (fieldMapInfo.getConverter() != null) {
				try {
					String prop = anno.getFeatureValueAsString(feat);
					ps.setObject(
							argIdx,
							fieldMapInfo.getConverter().convert(
									fieldMapInfo.getTargetType(), prop));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				if ("uima.cas.Integer".equals(feat.getRange().getName())) {
					ps.setInt(argIdx, anno.getIntValue(feat));
				} else if ("uima.cas.Short".equals(feat.getRange().getName())) {
					ps.setShort(argIdx, anno.getShortValue(feat));
				} else if ("uima.cas.Long".equals(feat.getRange().getName())) {
					ps.setLong(argIdx, anno.getLongValue(feat));
				} else if ("uima.cas.Float".equals(feat.getRange().getName())) {
					ps.setFloat(argIdx, anno.getFloatValue(feat));
				} else if ("uima.cas.Double".equals(feat.getRange().getName())) {
					ps.setDouble(argIdx, anno.getDoubleValue(feat));
				} else if ("uima.cas.Byte".equals(feat.getRange().getName())) {
					ps.setByte(argIdx, anno.getByteValue(feat));
				} else if ("uima.cas.Boolean".equals(feat.getRange().getName())) {
					ps.setBoolean(argIdx, anno.getBooleanValue(feat));
				} else if ("uima.cas.String".equals(feat.getRange().getName())) {
					String trunc = truncateString(anno.getStringValue(feat),
							fieldMapInfo.getSize());
					ps.setString(argIdx, trunc);
				}
			}
			argIdx++;
		}
	}

	private String truncateString(String val, int size) {
		String trunc = val;
		if (!Strings.isNullOrEmpty(val) && val.length() > size) {
			trunc = val.substring(0, size);
		}
		return trunc;
	}

	/**
	 * save the annotation properties
	 * 
	 * @param mapIdToAnno
	 * @param annoIds
	 */
	private void saveAnnoPrimitive(final Map<Integer, TOP> mapIdToAnno,
			Set<Integer> annoIds) {
		// nothing to do
		if (annoIds.size() == 0)
			return;
		// covert to array for spring batch update
		final Integer[] annoIdArray = annoIds.toArray(new Integer[] {});
		// get mappinginfo
		final Type type = mapIdToAnno.get(annoIdArray[0]).getType();
		final AnnoMappingInfo mapInfo = this.getMapInfo(type);
		// get non primitive fields, insert them after inserting the annotation
		final Set<String> fsNames = this.tl_mapFieldInfo.get().get(
				type.getName());
		final ListMultimap<String, AnnoFSAttribute> mapAnnoToFS = ArrayListMultimap
				.create();
		// don't know how to map this annotation
		if (mapInfo == null)
			return;
		jdbcTemplate.batchUpdate(mapInfo.getSql(),
				new BatchPreparedStatementSetter() {

					@Override
					public int getBatchSize() {
						return annoIdArray.length;
					}

					@Override
					public void setValues(PreparedStatement ps, int idx)
							throws SQLException {
						// get the entry
						int annoId = annoIdArray[idx];
						TOP anno = mapIdToAnno.get(annoId);
						saveAnnoBindVariables(type, mapInfo, ps, annoId, anno);
						// pull out the composit fields for storage
						for (String fieldName : fsNames) {
							Feature feat = type.getFeatureByBaseName(fieldName);
							if (feat.getRange().isArray()) {
								FSArray arr = (FSArray) anno
										.getFeatureValue(feat);
								if (arr != null) {
									for (int i = 0; i < arr.size(); i++) {
										FeatureStructure fs = arr.get(i);
										mapAnnoToFS.put(fs.getType().getName(),
												new AnnoFSAttribute(annoId, fs,
														i));

									}
								}
							} else {
								mapAnnoToFS.put(
										feat.getRange().getName(),
										new AnnoFSAttribute(annoId, anno
												.getFeatureValue(feat), null));
							}
						}
					}
				}

		);
		for (String fsType : mapAnnoToFS.keySet()) {
			this.saveAnnoFS(mapAnnoToFS.get(fsType));
		}
	}

	/**
	 * insert composite attributes. TODO: handle case where attribute is an
	 * annotation itself
	 * 
	 * @param listFSA
	 */
	private void saveAnnoFS(final List<AnnoFSAttribute> listFSA) {
		if (listFSA.size() == 0)
			return;
		final Type type = listFSA.get(0).getFs().getType();
		final AnnoMappingInfo mapInfo = this.getMapInfo(type);
		// don't know how to map this feature
		if (mapInfo == null)
			return;
		jdbcTemplate.batchUpdate(mapInfo.getSql(),
				new BatchPreparedStatementSetter() {

					@Override
					public int getBatchSize() {
						return listFSA.size();
					}

					@Override
					public void setValues(PreparedStatement ps, int idx)
							throws SQLException {
						AnnoFSAttribute fsa = listFSA.get(idx);
						// todo pass array index for storage
						saveAnnoBindVariables(type, mapInfo, ps,
								fsa.getAnnoBaseId(), fsa.getFs());
					}
				});
	}

	private void saveAnnotations(JCas jcas, Set<String> setTypesToIgnore,
			Set<String> typesStoreCoveredText, int coveredTextMaxLen,
			Document doc) {
		BiMap<TOP, Integer> mapAnnoToId = saveAnnoBase(jcas, setTypesToIgnore,
				typesStoreCoveredText, coveredTextMaxLen, doc);
		// split the annotations up by type
		// create a map of class name to anno id
		SetMultimap<String, Integer> mapTypeToAnnoId = HashMultimap.create();
		for (Map.Entry<TOP, Integer> annoEntry : mapAnnoToId.entrySet()) {
			mapTypeToAnnoId.put(annoEntry.getKey().getClass().getName(),
					annoEntry.getValue());
		}
		BiMap<Integer, TOP> mapIdToAnno = mapAnnoToId.inverse();
		// save
		for (String annoClass : mapTypeToAnnoId.keySet()) {
			saveAnnoPrimitive(mapIdToAnno, mapTypeToAnnoId.get(annoClass));
		}

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
			// communicate options to mappers using thread local variable
			MapperConfig.setConfig(typesStoreCoveredText, coveredTextMaxLen);
			Document doc = createDocument(jcas, analysisBatch, bStoreDocText,
					bStoreCAS);
			this.sessionFactory.getCurrentSession().save(doc);
			extractAndSaveDocKey(jcas, doc);
			this.sessionFactory.getCurrentSession().flush();
			saveAnnotations(jcas, setTypesToIgnore, typesStoreCoveredText,
					coveredTextMaxLen, doc);
			Query q = this.sessionFactory.getCurrentSession().getNamedQuery(
					"insertAnnotationContainmentLinks");
			q.setInteger("documentID", doc.getDocumentID());
			q.executeUpdate();
			return doc.getDocumentID();
		} finally {
			MapperConfig.unsetConfig();
		}
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setDbSchema(String dbSchema) {
		this.dbSchema = dbSchema;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setDialectClassName(String dialectClassName) {
		this.dialectClassName = dialectClassName;
		try {
			this.dialect = (Dialect) Class.forName(dialectClassName)
					.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setMapAnnoMappingInfo(
			Map<String, AnnoMappingInfo> mapAnnoMappingInfo) {
		this.mapAnnoMappingInfo = mapAnnoMappingInfo;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setYtexProperties(Properties ytexProperties) {
		this.ytexProperties = ytexProperties;
	}

}
