package ytex.kernel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public class AbstractBagOfWordsExporter {

	SimpleJdbcTemplate simpleJdbcTemplate;
	protected JdbcTemplate jdbcTemplate;

	public AbstractBagOfWordsExporter() {
		super();
	}

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(ds);
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
	protected void getNumericInstanceWords(String sql,
			final BagOfWordsData bagOfWordsData) {
		jdbcTemplate.query(sql, new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				int instanceId = rs.getInt(1);
				String word = rs.getString(2);
				double wordValue = rs.getDouble(3);
				addNumericWordToInstance(bagOfWordsData, instanceId, word,
						wordValue);
			}
		});
	}

	protected void addNumericWordToInstance(BagOfWordsData bagOfWordsData,
			int instanceId, String word, double wordValue) {
		// add the numeric word to the map of words for this document
		SortedMap<String, Double> words = bagOfWordsData
				.getInstanceNumericWords().get(instanceId);
		if (words == null) {
			words = new TreeMap<String, Double>();
			bagOfWordsData.getInstanceNumericWords().put(instanceId, words);
		}
		words.put(word, wordValue);
		bagOfWordsData.getNumericWords().add(word);
		// increment the length of the document by the wordValue
		Integer docLength = bagOfWordsData.getDocLengthMap().get(instanceId);
		if (docLength == null) {
			docLength = 0;
		}
		bagOfWordsData.getDocLengthMap().put(instanceId,
				(docLength + (int) wordValue));
		// add to the number of docs that have the word
		Integer docsWithWord = bagOfWordsData.getIdfMap().get(word);
		if (docsWithWord == null) {
			docsWithWord = 0;
		}
		bagOfWordsData.getIdfMap().put(word, docsWithWord + 1);
	}

	protected void addNominalWordToInstance(BagOfWordsData bagOfWordsData,
			int instanceId, String word, String wordValue) {
		SortedMap<String, String> instanceWords = bagOfWordsData
				.getInstanceNominalWords().get(instanceId);
		SortedSet<String> wordValueSet = bagOfWordsData
				.getNominalWordValueMap().get(word);
		if (instanceWords == null) {
			instanceWords = new TreeMap<String, String>();
			bagOfWordsData.getInstanceNominalWords().put(instanceId,
					instanceWords);
		}
		if (wordValueSet == null) {
			wordValueSet = new TreeSet<String>();
			bagOfWordsData.getNominalWordValueMap().put(word, wordValueSet);
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
	protected void getNominalInstanceWords(String sql,
			final BagOfWordsData bagOfWordsData) {
		jdbcTemplate.query(sql, new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				int instanceId = rs.getInt(1);
				String word = rs.getString(2);
				String wordValue = rs.getString(3);
				addNominalWordToInstance(bagOfWordsData, instanceId, word,
						wordValue);
			}
		});
	}

	/**
	 * update all values in bagOfWordsData.instanceNumericWords. apply tf-idf
	 * normalization.
	 * 
	 * @param bagOfWordsData
	 */
	protected void normalizeTfIDF(BagOfWordsData bagOfWordsData) {
		// iterate through all documents
		for (Map.Entry<Integer, SortedMap<String, Double>> instanceNumericWords : bagOfWordsData
				.getInstanceNumericWords().entrySet()) {
			int instanceId = instanceNumericWords.getKey();
			// iterate through all document attributes
			for (Map.Entry<String, Double> word : instanceNumericWords
					.getValue().entrySet()) {
				// term frequency within document
				double tf = word.getValue().doubleValue()
						/ bagOfWordsData.getDocLengthMap().get(instanceId);
				// inverse docuement frequency
				double idf = Math.log(bagOfWordsData.getIdfMap().size())
						- Math.log(bagOfWordsData.getIdfMap()
								.get(word.getKey()));
				// update
				word.setValue(tf * idf);
			}
		}
	}

	protected void loadProperties(String propertyFile, Properties props)
			throws FileNotFoundException, IOException,
			InvalidPropertiesFormatException {
		InputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			if (propertyFile.endsWith(".xml"))
				props.loadFromXML(in);
			else
				props.load(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	protected void loadData(BagOfWordsData bagOfWordsData,
			String instanceNumericWordQuery,
			String instanceNominalWordQuery, BagOfWordsDecorator bDecorator,
			boolean tfIdf) {
		if (instanceNumericWordQuery.trim().length() > 0)
			this.getNumericInstanceWords(instanceNumericWordQuery,
					bagOfWordsData);
		// added to support adding gram matrix index in GramMatrixExporter
		if (bDecorator != null)
			bDecorator.decorateNumericInstanceWords(bagOfWordsData
					.getInstanceNumericWords(), bagOfWordsData
					.getNumericWords());
		if (instanceNominalWordQuery.trim().length() > 0)
			this.getNominalInstanceWords(instanceNominalWordQuery,
					bagOfWordsData);
		if (bDecorator != null)
			bDecorator.decorateNominalInstanceWords(bagOfWordsData
					.getInstanceNominalWords(), bagOfWordsData
					.getNominalWordValueMap());
		if (tfIdf)
			this.normalizeTfIDF(bagOfWordsData);
	}
}