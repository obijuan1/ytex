package ytex.kernel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.FeatureInfogain;

/**
 * 
 * @author vijay
 * 
 */
public class InfoGainEvaluatorImpl implements InfoGainEvaluator {
	protected SimpleJdbcTemplate simpleJdbcTemplate;
	protected JdbcTemplate jdbcTemplate;
	protected PlatformTransactionManager transactionManager;
	protected TransactionTemplate txNew;
	protected ClassifierEvaluationDao classifierEvaluationDao;

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		txNew = new TransactionTemplate(transactionManager);
		txNew.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(ds);
	}

	public DataSource getDataSource(DataSource ds) {
		return this.jdbcTemplate.getDataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.InfoGainEvaluator#storeInfoGain(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void storeInfoGain(String name, String labelQuery,
			String featureQuery, String classFeatureQuery) {
		Map<String, Map<Integer, Map<String, Integer>>> labelClassMap = loadY(labelQuery);
		Map<String, Double> featureEntropyMap = loadFeatureEntropyMap(featureQuery);
		for (Map.Entry<String, Map<Integer, Map<String, Integer>>> labelClass : labelClassMap
				.entrySet()) {
			storeInfoGain(name, labelClass.getKey(), labelClass.getValue(),
					featureEntropyMap, classFeatureQuery);
		}
	}

	/**
	 * iterates through query results and computes infogain
	 * 
	 * @author vijay
	 * 
	 */
	public class InfoGainRowCallbackHandler implements RowCallbackHandler {
		private Map<Integer, List<FeatureInfogain>> foldInfogainMap;
		Integer currentFold;
		String currentFeature;
		/**
		 * matrix of class - feature bin - bin count. We don't know the
		 * dimensions of the matrix, so we use a map. Use a tree map so that we
		 * always iterate over this in the same order.
		 */
		Map<String, Map<String, Integer>> currentBins = new TreeMap<String, Map<String, Integer>>();
		Map<Integer, Map<String, Integer>> foldClassCountMap;
		Map<String, Double> featureEntropy;
		Map<Integer, Double> foldEntropy;

		public InfoGainRowCallbackHandler(
				Map<Integer, List<FeatureInfogain>> foldInfogainMap,
				Map<Integer, Map<String, Integer>> foldClassCountMap,
				Map<String, Double> featureEntropy,
				Map<Integer, Double> foldEntropy) {
			super();
			this.foldInfogainMap = foldInfogainMap;
			this.foldClassCountMap = foldClassCountMap;
			this.featureEntropy = featureEntropy;
			this.foldEntropy = foldEntropy;
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			Integer foldId = rs.getInt(1);
			String featureName = rs.getString(2);
			String className = rs.getString(3);
			String featureBin = rs.getString(4);
			int binCount = rs.getInt(5);
			if (foldId != currentFold || !currentFeature.equals(featureName)
					|| rs.isLast()) {
				if (currentFold != null)
					addInfoGain();
				// reinitialize state
				currentBins.clear();
				currentFold = foldId;
				currentFeature = featureName;
			}
			// add the current row to the bin matrix
			Map<String, Integer> featureBinMap = currentBins.get(className);
			if (featureBinMap == null) {
				featureBinMap = new TreeMap<String, Integer>();
				currentBins.put(className, featureBinMap);
			}
			featureBinMap.put(featureBin, binCount);
		}

		private void addInfoGain() {
			List<Integer> binCounts = new ArrayList<Integer>();
			int grandTotal = 0;
			List<Double> jointProbabilities = new ArrayList<Double>();
			// iterate over each class in the class-feature bin matrix
			for (Map.Entry<String, Map<String, Integer>> classFeatureBinMap : currentBins
					.entrySet()) {
				String className = classFeatureBinMap.getKey();
				Map<String, Integer> featureBinMap = classFeatureBinMap
						.getValue();
				// get the total number of instances for the specified class
				int classTotalCount = this.foldClassCountMap.get(currentFold)
						.get(className);
				// see if we are missing any
				int allFeatureBinCount = 0;
				for (int binCount : featureBinMap.values()) {
					// add the count for this bin to the list of bin counts
					binCounts.add(binCount);
					allFeatureBinCount += binCount;
				}
				// we didn't fill up the 'row' - add a count for the missing
				// feature bin
				if (allFeatureBinCount < classTotalCount) {
					binCounts.add(classTotalCount - allFeatureBinCount);
				}
			}
			// get the grand total
			for (int binCount : binCounts)
				grandTotal += binCount;
			// convert bin count into joint probability
			for (int binCount : binCounts)
				jointProbabilities.add((double) binCount / (double) grandTotal);
			// H(X) + H(Y) - H(X,Y)
			double infogain = this.featureEntropy.get(currentFeature)
					+ this.foldEntropy.get(currentFold)
					- entropy(jointProbabilities);
			List<FeatureInfogain> foldInfogainList = this.foldInfogainMap
					.get(currentFold);
			if (foldInfogainList == null) {
				foldInfogainList = new ArrayList<FeatureInfogain>();
				foldInfogainMap.put(currentFold, foldInfogainList);
			}
			foldInfogainList.add(new FeatureInfogain(null, null, currentFold,
					currentFeature, infogain, 0));
		}
	}

	private void storeInfoGain(final String name, final String label,
			final Map<Integer, Map<String, Integer>> foldClassCountMap,
			final Map<String, Double> featureEntropy, final String featureQuery) {
		final Map<Integer, List<FeatureInfogain>> foldInfogainMap = new HashMap<Integer, List<FeatureInfogain>>();
		final Map<Integer, Double> foldEntropy = this
				.calculateFoldEntropy(foldClassCountMap);
		final InfoGainRowCallbackHandler handler = new InfoGainRowCallbackHandler(
				foldInfogainMap, foldClassCountMap, featureEntropy, foldEntropy);
		txNew.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus txStatus) {
				jdbcTemplate.query(new PreparedStatementCreator() {

					@Override
					public PreparedStatement createPreparedStatement(
							Connection conn) throws SQLException {
						PreparedStatement ps = conn.prepareStatement(
								featureQuery, ResultSet.TYPE_FORWARD_ONLY,
								ResultSet.CONCUR_READ_ONLY);
						ps.setString(1, label);
						return ps;
					}

				}, handler);
				return null;
			}
		});
		// iterate over each feature list and rank the features
		for (List<FeatureInfogain> foldInfogainList : foldInfogainMap.values()) {
			// sort by infogain in descending order
			// if two features have the same infogain, order them by name
			Collections.sort(foldInfogainList,
					new Comparator<FeatureInfogain>() {

						@Override
						public int compare(FeatureInfogain o1,
								FeatureInfogain o2) {
							if (o1.getInfogain() > o2.getInfogain()) {
								return 1;
							} else if (o1.getInfogain() == o2.getInfogain()) {
								return o1.getFeatureName().compareTo(
										o2.getFeatureName());
							} else {
								return -1;
							}
						}
					});
			// update the rank of each infogain entry based on sorting
			int i = 1;
			for (FeatureInfogain ig : foldInfogainList) {
				ig.setRank(i++);
				ig.setName(name);
				ig.setLabel(label);
			}
			// insert the infogain
			classifierEvaluationDao.saveInfogain(foldInfogainList);
		}
	}

	private Map<String, Double> loadFeatureEntropyMap(final String labelQuery) {
		final Map<String, Double> featureEntropyMap = new HashMap<String, Double>();
		// fill in the y map from the query
		txNew.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				jdbcTemplate.query(labelQuery, new RowCallbackHandler() {
					String currentFeature = null;
					List<Double> currentFrequencies = new ArrayList<Double>();
					double currentTotalFreq = 0;

					/**
					 * iterate through results. get per-bin probabilities.
					 * calculate feature's entropy, put in featureEntropyMap
					 */
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String featureName = rs.getString(1);
						// String bin = rs.getString(2);
						double freq = rs.getDouble(3);
						if (!featureName.equals(currentFeature) || rs.isLast()) {
							if (currentFeature != null) {
								// new feature / end of list - update
								// make sure the frequency adds up to 1
								currentFrequencies.add(1d - currentTotalFreq);
								featureEntropyMap.put(currentFeature,
										entropy(currentFrequencies));
							}
							// reset state
							currentFrequencies.clear();
							currentTotalFreq = 0;
							currentFeature = featureName;
						}
						// update current feature
						currentFrequencies.add(freq);
						currentTotalFreq += freq;
					}
				});
				return null;
			}

		});
		return featureEntropyMap;
	}

	/**
	 * 
	 * @param labelQuery
	 * @return Map[Label, Map[Fold Id, Map[Class, Count]]]
	 */
	private Map<String, Map<Integer, Map<String, Integer>>> loadY(
			final String labelQuery) {
		final Map<String, Map<Integer, Map<String, Integer>>> y = new HashMap<String, Map<Integer, Map<String, Integer>>>();
		// fill in the y map from the query
		txNew.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus txStatus) {
				jdbcTemplate.query(labelQuery, new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String label = rs.getString(1);
						int foldId = rs.getInt(2);
						String cls = rs.getString(3);
						int count = rs.getInt(4);
						Map<Integer, Map<String, Integer>> foldToClassMap = y
								.get(label);
						if (foldToClassMap == null) {
							foldToClassMap = new HashMap<Integer, Map<String, Integer>>();
							y.put(label, foldToClassMap);
						}
						Map<String, Integer> classToCountMap = foldToClassMap
								.get(foldId);
						if (classToCountMap == null) {
							classToCountMap = new HashMap<String, Integer>();
							foldToClassMap.put(foldId, classToCountMap);
						}
						classToCountMap.put(cls, count);
					}
				});
				return null;
			}
		});
		return y;
	}

	/**
	 * 
	 * @param pY
	 * @return Map[FoldId, H(Y)]
	 */
	private Map<Integer, Double> calculateFoldEntropy(
			Map<Integer, Map<String, Integer>> foldClassMap) {
		// allocate map to hold per-fold entropy
		Map<Integer, Double> foldEntropy = new HashMap<Integer, Double>(
				foldClassMap.size());
		// iterate over folds
		for (Map.Entry<Integer, Map<String, Integer>> foldClass : foldClassMap
				.entrySet()) {
			Map<String, Integer> classCountMap = foldClass.getValue();
			int total = 0;
			List<Double> classProbs = new ArrayList<Double>(
					classCountMap.size());
			// calculate total number of instances in this fold
			for (Integer classCount : classCountMap.values()) {
				total += classCount;
			}
			// calculate per-class probability in this fold
			for (Integer classCount : classCountMap.values()) {
				classProbs.add((double) classCount / (double) total);
			}
			// calculate entropy, save
			foldEntropy.put(foldClass.getKey(), entropy(classProbs));
		}
		return foldEntropy;
	}

	/**
	 * calculate entropy from a list/array of probabilities
	 * 
	 * @param classProbs
	 * @return
	 */
	private double entropy(Iterable<Double> classProbs) {
		double entropy = 0;
		double log2 = Math.log(2);
		for (double prob : classProbs) {
			entropy += prob * Math.log(prob) / log2;
		}
		return entropy * -1;
	}

	@SuppressWarnings("static-access")
	public static void main(String args[]) throws ParseException, IOException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("prop")
				.hasArg()
				.isRequired()
				.withDescription(
						"property file with queries and other parameters")
				.create("prop"));
		try {
			if (args.length == 0)
				printHelp(options);
			else {
				CommandLineParser parser = new GnuParser();
				CommandLine line = parser.parse(options, args);
				String propFile = line.getOptionValue("prop");
				InputStream is = null;
				String name;
				String labelQuery;
				String featureQuery;
				String classFeatureQuery;
				try {
					is = new FileInputStream(propFile);
					Properties props = new Properties();
					if (propFile.endsWith(".xml"))
						props.loadFromXML(is);
					else
						props.load(is);
					name = props.getProperty("infogain.name");
					labelQuery = props.getProperty("label.query");
					featureQuery = props.getProperty("feature.query");
					classFeatureQuery = props.getProperty("classfeature.query");
				} finally {
					if (is != null)
						is.close();
				}
				if (labelQuery != null && name != null && featureQuery != null
						&& classFeatureQuery != null) {
					KernelContextHolder
							.getApplicationContext()
							.getBean(InfoGainEvaluator.class)
							.storeInfoGain(name, labelQuery, featureQuery,
									classFeatureQuery);
				} else {
					printHelp(options);
				}
			}
		} catch (ParseException pe) {
			printHelp(options);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + InfoGainEvaluatorImpl.class.getName()
				+ " calculate infogain for each feature", options);
	}

}