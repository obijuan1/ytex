package ytex.weka;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;

public class BagOfWordsExporterImpl implements BagOfWordsExporter {
	SimpleJdbcTemplate simpleJdbcTemplate;
	JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(ds);
	}

	public DataSource getDataSource(DataSource ds) {
		return this.jdbcTemplate.getDataSource();
	}

	/**
	 * initialize the weka Instances
	 * 
	 * @param arffRelation
	 * @param sql
	 * @param classLabels
	 * @return
	 */
	private Instances initializeInstances(String arffRelation,
			Set<String> classLabels, Set<String> numericWords,
			Map<String, Set<String>> nominalWords,
			BagOfWordsDecorator bDecorator) {
		FastVector wekaAttributes = new FastVector(numericWords.size()
				+ nominalWords.size() + 2);
		// add instance id attribute
		wekaAttributes.addElement(new Attribute("instance_id"));
		// add numeric word attributes
		for (String word : numericWords) {
			Attribute attribute = new Attribute(word);
			wekaAttributes.addElement(attribute);
		}
		// add nominal word attributes
		for (Map.Entry<String, Set<String>> nominalWordEntry : nominalWords
				.entrySet()) {
			FastVector wordValues = new FastVector(nominalWordEntry.getValue()
					.size());
			for (String wordValue : nominalWordEntry.getValue()) {
				wordValues.addElement(wordValue);
			}
			Attribute attribute = new Attribute(nominalWordEntry.getKey(),
					wordValues);
			wekaAttributes.addElement(attribute);
		}
		// add class attribute
		FastVector wekaClassLabels = new FastVector(classLabels.size());
		for (String classLabel : classLabels) {
			wekaClassLabels.addElement(classLabel);
		}
		wekaAttributes.addElement(new Attribute("class", wekaClassLabels));
		Instances instances = new Instances(arffRelation, wekaAttributes, 0);
		instances.setClassIndex(instances.numAttributes() - 1);
		return instances;
	}

	/**
	 * 
	 * @param sql
	 *            result 1st column: instance id, 2nd column: word, 3rd column:
	 *            numeric word value
	 * @param instanceNumericWords
	 *            map of instance id - [map word - word value] to be populated
	 */
	protected void getNumericInstanceWords(String sql,
			final Map<Integer, Map<String, Double>> instanceNumericWords,
			final Set<String> numericWordSet) {
		jdbcTemplate.query(sql, new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				int instanceId = rs.getInt(1);
				String word = rs.getString(2);
				double wordValue = rs.getDouble(3);
				addNumericWordToInstance(instanceNumericWords, numericWordSet,
						instanceId, word, wordValue);
			}
		});
	}

	private void addNumericWordToInstance(
			final Map<Integer, Map<String, Double>> instanceNumericWords,
			final Set<String> numericWordSet, int instanceId, String word,
			double wordValue) {
		Map<String, Double> words = instanceNumericWords.get(instanceId);
		if (words == null) {
			words = new HashMap<String, Double>();
			instanceNumericWords.put(instanceId, words);
		}
		words.put(word, wordValue);
		numericWordSet.add(word);
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
	private void getNominalInstanceWords(String sql,
			final Map<Integer, Map<String, String>> instanceWordMap,
			final Map<String, Set<String>> wordValueMap) {
		jdbcTemplate.query(sql, new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				int instanceId = rs.getInt(1);
				String word = rs.getString(2);
				String wordValue = rs.getString(3);
				addNominalWordToInstance(instanceWordMap, wordValueMap,
						instanceId, word, wordValue);
			}
		});
	}

	private void addNominalWordToInstance(
			final Map<Integer, Map<String, String>> instanceWordMap,
			final Map<String, Set<String>> wordValueMap, int instanceId,
			String word, String wordValue) {
		Map<String, String> instanceWords = instanceWordMap.get(instanceId);
		Set<String> wordValueSet = wordValueMap.get(word);
		if (instanceWords == null) {
			instanceWords = new HashMap<String, String>();
			instanceWordMap.put(instanceId, instanceWords);
		}
		if (wordValueSet == null) {
			wordValueSet = new TreeSet<String>();
			wordValueMap.put(word, wordValueSet);
		}
		// add the word-value for the instance
		instanceWords.put(word, wordValue);
		// add the value to the set of valid values
		wordValueSet.add(wordValue);
	}

	private void getInstances(String sql,
			final Map<Integer, String> instanceClasses,
			final Set<String> classes, final Set<String> numericWords,
			final Map<Integer, Map<String, Double>> instanceNumericWords,
			final Map<String, Set<String>> nominalWordValueMap,
			final Map<Integer, Map<String, String>> instanceNominalWords) {
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			private Set<String> numericColumnHeaders;
			private Set<String> nominalColumnHeaders;

			private void initMetaData(ResultSet rs) throws SQLException {
				if (numericColumnHeaders == null) {
					numericColumnHeaders = new HashSet<String>();
					nominalColumnHeaders = new HashSet<String>();

					ResultSetMetaData rsmd = rs.getMetaData();
					for (int i = 3; i <= rsmd.getColumnCount(); i++) {
						int colType = rsmd.getColumnType(i);
						if (colType == Types.CHAR || colType == Types.BOOLEAN
								|| colType == Types.VARCHAR) {
							nominalColumnHeaders.add(rsmd.getColumnLabel(i));
						} else if (colType == Types.DECIMAL
								|| colType == Types.BIGINT
								|| colType == Types.DOUBLE
								|| colType == Types.FLOAT
								|| colType == Types.DECIMAL
								|| colType == Types.INTEGER
								|| colType == Types.NUMERIC
								|| colType == Types.REAL) {
							numericColumnHeaders.add(rsmd.getColumnLabel(i));
						}
					}
				}

			}

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				this.initMetaData(rs);
				int instanceId = rs.getInt(1);
				String classLabel = rs.getString(2);
				instanceClasses.put(instanceId, classLabel);
				classes.add(classLabel);
				// add other attributes
				for (String columnHeader : this.numericColumnHeaders) {
					double wordValue = rs.getDouble(columnHeader);
					if (!rs.wasNull()) {
						addNumericWordToInstance(instanceNumericWords,
								numericWords, instanceId, columnHeader,
								wordValue);
					}
				}
				for (String columnHeader : this.nominalColumnHeaders) {
					String wordValue = rs.getString(columnHeader);
					if (!rs.wasNull()) {
						addNominalWordToInstance(instanceNominalWords,
								nominalWordValueMap, instanceId, columnHeader,
								wordValue);
					}
				}

			}
		});
	}

	private void addWordsToInstances(Instances instances,
			Map<Integer, Map<String, Double>> instanceNumericWords,
			Map<Integer, Map<String, String>> instanceNominalWords,
			Map<Integer, String> documentClasses) throws IOException {
		for (Map.Entry<Integer, String> entry : documentClasses.entrySet()) {
			double[] zeroValues = new double[instances.numAttributes()];
			Arrays.fill(zeroValues, 0.0d);
			SparseInstance wekaInstance = new SparseInstance(1.0d, zeroValues);
			wekaInstance.setDataset(instances);
			// set instance id
			Attribute instanceId = instances.attribute("instance_id");
			wekaInstance.setValue(instanceId.index(), entry.getKey()
					.doubleValue());
			// set document class
			Attribute classAttr = instances.attribute("class");
			wekaInstance.setValue(classAttr.index(), classAttr
					.indexOfValue(entry.getValue()));
			// set numeric words
			if (instanceNumericWords.get(entry.getKey()) != null) {
				for (Map.Entry<String, Double> word : instanceNumericWords.get(
						entry.getKey()).entrySet()) {
					Attribute wordAttr = instances.attribute(word.getKey());
					wekaInstance.setValue(wordAttr.index(), word.getValue()
							.doubleValue());
				}
			}
			// set nominal words
			if (instanceNominalWords.get(entry.getKey()) != null) {
				for (Map.Entry<String, String> word : instanceNominalWords.get(
						entry.getKey()).entrySet()) {
					Attribute wordAttr = instances.attribute(word.getKey());
					int valueIndex = wordAttr.indexOfValue(word.getValue());
					if (valueIndex == -1) {
						throw new IOException("oops! " + word);
					}
					wekaInstance.setValue(wordAttr.index(), valueIndex);
				}
			}
			instances.add(wekaInstance);
		}
	}

	public void exportBagOfWords(String arffRelation,
			String instanceClassQuery, String instanceNumericWordQuery,
			String instanceNominalWordQuery, BufferedWriter writer)
			throws IOException {
		exportBagOfWords(arffRelation, instanceClassQuery,
				instanceNumericWordQuery, instanceNominalWordQuery, writer,
				null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.yale.cbb.uima.weka.BagOfWordsExporter#exportBagOfWords(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String,
	 * java.io.BufferedWriter)
	 */
	public void exportBagOfWords(String arffRelation,
			String instanceClassQuery, String instanceNumericWordQuery,
			String instanceNominalWordQuery, BufferedWriter writer,
			BagOfWordsDecorator bDecorator) throws IOException {
		Map<Integer, String> documentClasses = new HashMap<Integer, String>();
		Set<String> classes = new TreeSet<String>();
		// get numeric words for each instance
		Set<String> numericWords = new TreeSet<String>();
		Map<Integer, Map<String, Double>> instanceNumericWords = new HashMap<Integer, Map<String, Double>>();
		// get nominal words for each instance
		Map<Integer, Map<String, String>> instanceNominalWords = new HashMap<Integer, Map<String, String>>();
		Map<String, Set<String>> nominalWordValueMap = new TreeMap<String, Set<String>>();
		// get instance ids and their classes
		this.getInstances(instanceClassQuery, documentClasses, classes,
				numericWords, instanceNumericWords, nominalWordValueMap,
				instanceNominalWords);
		if (instanceNumericWordQuery.trim().length() > 0)
			this.getNumericInstanceWords(instanceNumericWordQuery,
					instanceNumericWords, numericWords);
		// added to support adding gram matrix index in GramMatrixExporter
		if (bDecorator != null)
			bDecorator.decorateNumericInstanceWords(instanceNumericWords,
					numericWords);
		if (instanceNominalWordQuery.trim().length() > 0)
			this.getNominalInstanceWords(instanceNominalWordQuery,
					instanceNominalWords, nominalWordValueMap);
		if (bDecorator != null)
			bDecorator.decorateNominalInstanceWords(instanceNominalWords,
					nominalWordValueMap);
		// add instance for each document
		// initialize the instances
		Instances instances = initializeInstances(arffRelation, classes,
				numericWords, nominalWordValueMap, bDecorator);
		this.addWordsToInstances(instances, instanceNumericWords,
				instanceNominalWords, documentClasses);
		writer.write(instances.toString());
	}

	public void exportBagOfWords(String propertyFile) throws IOException {
		exportBagOfWords(propertyFile, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.yale.cbb.uima.weka.BagOfWordsExporter#exportBagOfWords(java.lang.
	 * String)
	 */
	public void exportBagOfWords(String propertyFile,
			BagOfWordsDecorator bDecorator) throws IOException {
		Properties props = new Properties();
		BufferedWriter writer = null;
		InputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			if (propertyFile.endsWith(".xml"))
				props.loadFromXML(in);
			else
				props.load(in);
			writer = new BufferedWriter(new FileWriter(props
					.getProperty("arffFile")));
			exportBagOfWords(props.getProperty("arffRelation"), props
					.getProperty("instanceClassQuery"), props.getProperty(
					"numericWordQuery", ""), props.getProperty(
					"nominalWordQuery", ""), writer, bDecorator);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
			}
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e) {
			}
		}
	}
}
