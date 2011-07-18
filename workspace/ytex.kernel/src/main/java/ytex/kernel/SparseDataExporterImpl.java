package ytex.kernel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class SparseDataExporterImpl implements SparseDataExporter {

	protected SimpleJdbcTemplate simpleJdbcTemplate;
	protected JdbcTemplate jdbcTemplate;
	protected NamedParameterJdbcTemplate namedJdbcTemplate;
	protected PlatformTransactionManager transactionManager;
	protected TransactionTemplate txNew;
	protected Map<String, SparseDataFormatterFactory> nameToFormatterMap = new HashMap<String, SparseDataFormatterFactory>();

	protected KernelUtil kernelUtil;

	private static final Log log = LogFactory
			.getLog(SparseDataExporterImpl.class);

	public Map<String, SparseDataFormatterFactory> getNameToFormatterMap() {
		return nameToFormatterMap;
	}

	public void setNameToFormatterMap(
			Map<String, SparseDataFormatterFactory> nameToFormatterMap) {
		this.nameToFormatterMap = nameToFormatterMap;
	}

	public KernelUtil getKernelUtil() {
		return kernelUtil;
	}

	public void setKernelUtil(KernelUtil kernelUtil) {
		this.kernelUtil = kernelUtil;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		txNew = new TransactionTemplate(transactionManager);
		txNew.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
	}

	public SparseDataExporterImpl() {
		super();
	}

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(ds);
		this.namedJdbcTemplate = new NamedParameterJdbcTemplate(ds);
	}

	public DataSource getDataSource(DataSource ds) {
		return this.jdbcTemplate.getDataSource();
	}

	/**
	 * 
	 * @param sql
	 *            result 1st column: instance id, 2nd column: word, 3rd column:
	 *            numeric word value
	 * @param instanceNumericWords
	 *            map of instance id - [map word - word value] to be populated
	 */
	protected void getNumericInstanceWords(final String sql,
			final SparseData sparseData, final Map<String, Object> params) {
		txNew.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus txStatus) {
				namedJdbcTemplate.query(sql, params
				// new PreparedStatementCreator() {
				//
				// @Override
				// public PreparedStatement createPreparedStatement(
				// Connection conn) throws SQLException {
				// return conn.prepareStatement(sql,
				// ResultSet.TYPE_FORWARD_ONLY,
				// ResultSet.CONCUR_READ_ONLY);
				// }
				//
				// }
						, new RowCallbackHandler() {

							@Override
							public void processRow(ResultSet rs)
									throws SQLException {
								int instanceId = rs.getInt(1);
								String word = rs.getString(2);
								double wordValue = rs.getDouble(3);
								addNumericWordToInstance(sparseData,
										instanceId, word, wordValue);
							}
						});
				return null;
			}

		});
	}

	protected void addNumericWordToInstance(SparseData sparseData,
			long instanceId, String word, double wordValue) {
		// add the instance id to the set of instance ids if necessary
		if (!sparseData.getInstanceIds().contains(instanceId))
			sparseData.getInstanceIds().add(instanceId);
		// add the numeric word to the map of words for this document
		SortedMap<String, Double> words = sparseData.getInstanceNumericWords()
				.get(instanceId);
		if (words == null) {
			words = new TreeMap<String, Double>();
			sparseData.getInstanceNumericWords().put(instanceId, words);
		}
		words.put(word, wordValue);
		sparseData.getNumericWords().add(word);
	}

	protected void addNominalWordToInstance(SparseData sparseData,
			long instanceId, String word, String wordValue) {
		// add the instance id to the set of instance ids if necessary
		if (!sparseData.getInstanceIds().contains(instanceId))
			sparseData.getInstanceIds().add(instanceId);
		SortedMap<String, String> instanceWords = sparseData
				.getInstanceNominalWords().get(instanceId);
		SortedSet<String> wordValueSet = sparseData.getNominalWordValueMap()
				.get(word);
		if (instanceWords == null) {
			instanceWords = new TreeMap<String, String>();
			sparseData.getInstanceNominalWords().put(instanceId, instanceWords);
		}
		if (wordValueSet == null) {
			wordValueSet = new TreeSet<String>();
			sparseData.getNominalWordValueMap().put(word, wordValueSet);
		}
		// add the word-value for the instance
		instanceWords.put(word, wordValue);
		// add the value to the set of valid values
		wordValueSet.add(wordValue);
	}

	/**
	 * 
	 * @param sql
	 *            result set has 3 columns. 1st column - integer - instance id.
	 *            2nd column - word. 3rd column - word value.
	 * @param instanceWordMap
	 *            map of instance id to word-word value.
	 * @param wordValueMap
	 *            map of word to valid values for the word.
	 * @return populate maps with results of query.
	 */
	protected void getNominalInstanceWords(final String sql,
			final SparseData sparseData, final Map<String, Object> params) {
		txNew.execute(new TransactionCallback<Object>() {

			// new PreparedStatementCreator() {
			// @Override

			// public PreparedStatement createPreparedStatement(
			// Connection conn) throws SQLException {
			// return conn.prepareStatement(sql,
			// ResultSet.TYPE_FORWARD_ONLY,
			// ResultSet.CONCUR_READ_ONLY);
			// }
			//
			// } @Override
			public Object doInTransaction(TransactionStatus txStatus) {
				namedJdbcTemplate.query(sql, params, new RowCallbackHandler() {

					@Override
					public void processRow(ResultSet rs) throws SQLException {
						int instanceId = rs.getInt(1);
						String word = rs.getString(2);
						String wordValue = rs.getString(3);
						addNominalWordToInstance(sparseData, instanceId, word,
								wordValue);
					}
				});
				return null;
			}
		});
	}

	protected SparseData loadData(InstanceData instanceLabel,
			String instanceNumericWordQuery, String instanceNominalWordQuery,
			BagOfWordsDecorator bDecorator, String label, Integer fold,
			Integer run) {
		SparseData sparseData = new SparseData();
		Map<String, Object> params = new HashMap<String, Object>();
		if (label != null && label.length() > 0)
			params.put("label", label);
		if (fold != null && fold != 0)
			params.put("fold", fold);
		if (run != null && run != 0)
			params.put("run", run);
		// load numeric attributes
		if (instanceNumericWordQuery != null
				&& instanceNumericWordQuery.trim().length() > 0)
			this.getNumericInstanceWords(instanceNumericWordQuery, sparseData,
					params);
		// added to support adding gram matrix index in GramMatrixExporter
		if (bDecorator != null)
			bDecorator.decorateNumericInstanceWords(
					sparseData.getInstanceNumericWords(),
					sparseData.getNumericWords());
		// load nominal attributes
		if (instanceNominalWordQuery != null
				&& instanceNominalWordQuery.trim().length() > 0)
			this.getNominalInstanceWords(instanceNominalWordQuery, sparseData,
					params);
		if (bDecorator != null)
			bDecorator.decorateNominalInstanceWords(
					sparseData.getInstanceNominalWords(),
					sparseData.getNominalWordValueMap());
		return sparseData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.SparseDataExporter#exportData(ytex.kernel.SparseData,
	 * ytex.kernel.SparseDataFormatter, java.util.Properties)
	 */
	public void exportData(InstanceData instanceLabel,
			SparseDataFormatter formatter, Properties properties,
			BagOfWordsDecorator bDecorator) throws IOException {
		String scope = properties.getProperty("scope", null);
		SparseData sparseData = null;
		if (scope == null) {
			sparseData = this.loadData(instanceLabel,
					properties.getProperty("numericWordQuery"),
					properties.getProperty("nominalWordQuery"), bDecorator,
					null, null, null);
		}
		formatter.initializeExport(instanceLabel, properties, sparseData);
		for (String label : instanceLabel.getLabelToInstanceMap().keySet()) {
			if ("label".equals(scope)) {
				sparseData = this.loadData(instanceLabel,
						properties.getProperty("numericWordQuery"),
						properties.getProperty("nominalWordQuery"), bDecorator,
						label, null, null);
			}
			formatter
					.initializeLabel(label, instanceLabel
							.getLabelToInstanceMap().get(label), properties,
							sparseData);
			for (int run : instanceLabel.getLabelToInstanceMap().get(label)
					.keySet()) {
				for (int fold : instanceLabel.getLabelToInstanceMap()
						.get(label).get(run).keySet()) {
					if (log.isInfoEnabled()
							&& (label.length() > 0 || run > 0 || fold > 0))
						log.info("exporting, label " + label + " run " + run
								+ " fold " + fold);
					if ("fold".equals(scope)) {
						sparseData = this.loadData(instanceLabel,
								properties.getProperty("numericWordQuery"),
								properties.getProperty("nominalWordQuery"),
								bDecorator, label, fold, run);
					}
					formatter.initializeFold(sparseData, label, run, fold,
							instanceLabel.getLabelToInstanceMap().get(label)
									.get(run).get(fold));
					for (boolean train : instanceLabel.getLabelToInstanceMap()
							.get(label).get(run).get(fold).keySet()) {
						formatter.exportFold(sparseData, instanceLabel
								.getLabelToInstanceMap().get(label).get(run)
								.get(fold).get(train), train, label,
								0 == run ? null : run, 0 == fold ? null : fold);
					}
					formatter.clearFold();
				}
			}
			formatter.clearLabel();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.SparseDataExporter#exportData(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void exportData(String propertiesFile, String format)
			throws IOException, InvalidPropertiesFormatException {
		Properties props = new Properties();
		this.getKernelUtil().loadProperties(propertiesFile, props);
		this.exportData(props, nameToFormatterMap.get(format.toLowerCase())
				.getFormatter(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.SparseDataExporter#exportData(java.util.Properties,
	 * ytex.kernel.SparseDataFormatter, ytex.kernel.BagOfWordsDecorator)
	 */
	@Override
	public void exportData(Properties props, SparseDataFormatter formatter,
			BagOfWordsDecorator bDecorator) throws IOException {
		InstanceData instanceLabel = this.getKernelUtil().loadInstances(
				props.getProperty("instanceClassQuery"));
		// load label - instance id maps
		// sparseData.setLabelToInstanceMap(this.getKernelUtil().loadInstances(
		// props.getProperty("instanceClassQuery"),
		// sparseData.getLabelToClassMap()));
		this.exportData(instanceLabel, formatter, props, bDecorator);
		// this.loadData(sparseData,
		// props.getProperty("numericWordQuery"),
		// props.getProperty("nominalWordQuery"), bDecorator);
		// this.exportData(sparseData, formatter, props);
	}

	@SuppressWarnings("static-access")
	public static void main(String args[]) throws IOException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("prop")
				.hasArg()
				.isRequired()
				.withDescription(
						"property file with queries and other parameters.")
				.create("prop"));
		options.addOption(OptionBuilder.withArgName("type").hasArg()
				.isRequired()
				.withDescription("export format; valid values: weka, libsvm")
				.create("type"));
		if (args.length == 0)
			printHelp(options);
		else {
			try {
				CommandLineParser parser = new GnuParser();
				CommandLine line = parser.parse(options, args);
				String propFile = line.getOptionValue("prop");
				String format = line.getOptionValue("type");
				SparseDataExporter exporter = KernelContextHolder
						.getApplicationContext().getBean(
								SparseDataExporter.class);
				exporter.exportData(propFile, format);
			} catch (ParseException pe) {
				printHelp(options);
			}
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();

		formatter.printHelp("java " + SparseDataExporterImpl.class.getName()
				+ " export sparse data", options);
	}
}