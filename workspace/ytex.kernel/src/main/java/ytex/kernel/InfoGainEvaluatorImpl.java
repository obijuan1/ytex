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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureRank;

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
	private static final Log log = LogFactory
			.getLog(InfoGainEvaluatorImpl.class);
	private static final String FEATURE_EVAL_TYPE = "infogain";

	public static class FeatureInfo {
		double entropy;
		SortedMap<String, Double> binToFrequencyMap = new TreeMap<String, Double>();

		public double getEntropy() {
			return entropy;
		}

		public void setEntropy(double entropy) {
			this.entropy = entropy;
		}

		public SortedMap<String, Double> getBinToFrequencyMap() {
			return binToFrequencyMap;
		}

		public void setBinToFrequencyMap(
				SortedMap<String, Double> binToFrequencyMap) {
			this.binToFrequencyMap = binToFrequencyMap;
		}
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		txNew = new TransactionTemplate(transactionManager);
		txNew
				.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
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
			String featureQuery, String classFeatureQuery, Double minInfo) {
		// delete existing feature evaluations with this name
		this.classifierEvaluationDao.deleteFeatureEvaluationByNameAndType(name,
				FEATURE_EVAL_TYPE);
		// load Y - class distributions per fold & label
		Map<String, Map<Integer, Map<String, Integer>>> labelClassMap = loadY(labelQuery);
		// load X - feature distributions per bin & entropy
		Map<String, FeatureInfo> featureInfoMap = loadFeatureInfoMap(featureQuery);
		// process each label
		for (Map.Entry<String, Map<Integer, Map<String, Integer>>> labelClass : labelClassMap
				.entrySet()) {
			storeInfoGain(name, labelClass.getKey(), labelClass.getValue(),
					featureInfoMap, classFeatureQuery, minInfo == null ? 0
							: minInfo);
		}
	}

	/**
	 * iterates through query results and computes infogain
	 * 
	 * @author vijay
	 * 
	 */
	@SuppressWarnings("unchecked")
	public class InfoGainResultSetExtractor implements ResultSetExtractor {
		private Map<Integer, List<FeatureRank>> foldInfogainMap;
		int currentFold;
		String currentFeature;
		double minInfo;
		/**
		 * matrix of class - feature bin - bin count. We don't know the
		 * dimensions of the matrix, so we use a map. Use a tree map so that we
		 * always iterate over this in the same order.
		 */
		Map<String, Map<String, Integer>> currentBins = new TreeMap<String, Map<String, Integer>>();
		Map<Integer, Map<String, Integer>> foldClassCountMap;
		Map<String, FeatureInfo> featureInfoMap;
		Map<Integer, Double> foldEntropy;

		public InfoGainResultSetExtractor(
				Map<Integer, List<FeatureRank>> foldInfogainMap,
				Map<Integer, Map<String, Integer>> foldClassCountMap,
				Map<String, FeatureInfo> featureInfoMap,
				Map<Integer, Double> foldEntropy, double minInfo) {
			super();
			this.foldInfogainMap = foldInfogainMap;
			this.foldClassCountMap = foldClassCountMap;
			this.featureInfoMap = featureInfoMap;
			this.foldEntropy = foldEntropy;
		}

		public void processRow(ResultSet rs) throws SQLException {
			Integer foldId = rs.getInt(1);
			String featureName = rs.getString(2).toLowerCase();
			String className = rs.getString(3).toLowerCase();
			String featureBin = rs.getString(4).toLowerCase();
			int binCount = rs.getInt(5);
			if (foldId != currentFold || !featureName.equals(currentFeature)) {
				if (currentFeature != null)
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
			if (!this.featureInfoMap.containsKey(currentFeature.toLowerCase())) {
				log
						.warn("bins and marginal probabilities for feature not defined, skipping; featureName="
								+ currentFeature);
				return;
			}
			List<Integer> binCounts = new ArrayList<Integer>();
			int grandTotal = 0;
			List<Double> jointProbabilities = new ArrayList<Double>();
			for (Map.Entry<String, Integer> classNameCount : this.foldClassCountMap
					.get(this.currentFold).entrySet()) {
				// iterate over 'rows' i.e. the class names
				String className = classNameCount.getKey();
				int classCount = classNameCount.getValue();
				grandTotal += classCount;
				// keep track of how many instances have already been allocated
				// to a feature bin
				int classFeatureCount = 0;
				for (String binName : this.featureInfoMap.get(
						currentFeature.toLowerCase()).getBinToFrequencyMap()
						.keySet()) {
					// iterate over 'columns' i.e. the feature bins
					int binCount = 0;
					if (currentBins.containsKey(className)
							&& currentBins.get(className).containsKey(binName))
						binCount = currentBins.get(className).get(binName);
					classFeatureCount += binCount;
					binCounts.add(binCount);
				}
				// add a trailing bin for when the feature is not present
				binCounts.add(classCount - classFeatureCount);
			}
			// convert bin count into joint probability
			for (int binCount : binCounts)
				jointProbabilities.add((double) binCount / (double) grandTotal);
			// H(X) + H(Y) - H(X,Y)
			double infogain = this.featureInfoMap.get(currentFeature)
					.getEntropy()
					+ this.foldEntropy.get(currentFold)
					- entropy(jointProbabilities);
			if (infogain > minInfo) {
				List<FeatureRank> foldInfogainList = this.foldInfogainMap
						.get(currentFold);
				if (foldInfogainList == null) {
					foldInfogainList = new ArrayList<FeatureRank>();
					foldInfogainMap.put(currentFold, foldInfogainList);
				}
				foldInfogainList.add(new FeatureRank(currentFeature, infogain));
			}
		}

		@Override
		public Object extractData(ResultSet rs) throws SQLException,
				DataAccessException {
			while (rs.next()) {
				this.processRow(rs);
			}
			// if we hit the end, add the info gain
			if (currentFeature != null)
				addInfoGain();
			return null;
		}
	}

	private void storeInfoGain(final String name, final String label,
			final Map<Integer, Map<String, Integer>> foldClassCountMap,
			final Map<String, FeatureInfo> featureInfoMap,
			final String featureQuery, double minInfo) {
		if(log.isDebugEnabled())
			log.debug("processing label: " + label);
		final Map<Integer, List<FeatureRank>> foldInfogainMap = new HashMap<Integer, List<FeatureRank>>();
		final Map<Integer, Double> foldEntropy = this
				.calculateFoldEntropy(foldClassCountMap);
		final InfoGainResultSetExtractor handler = new InfoGainResultSetExtractor(
				foldInfogainMap, foldClassCountMap, featureInfoMap,
				foldEntropy, minInfo);
		txNew.execute(new TransactionCallback<Object>() {

			@SuppressWarnings("unchecked")
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
		for (Map.Entry<Integer, List<FeatureRank>> foldInfogain : foldInfogainMap
				.entrySet()) {
			// sort by infogain in descending order
			List<FeatureRank> foldInfogainList = foldInfogain.getValue();
			int foldId = foldInfogain.getKey();
			Collections.sort(foldInfogainList,
					new FeatureRank.FeatureRankDesc());
			// update the rank of each infogain entry based on sorting
			int i = 1;
			for (FeatureRank ig : foldInfogainList) {
				ig.setRank(i++);
			}
			FeatureEvaluation featureEval = new FeatureEvaluation(name, label,
					foldId, FEATURE_EVAL_TYPE, foldInfogainList);
			// insert the infogain
			classifierEvaluationDao.saveFeatureEvaluation(featureEval);
		}
	}

	private Map<String, FeatureInfo> loadFeatureInfoMap(final String labelQuery) {
		final Map<String, FeatureInfo> featureInfoMap = new HashMap<String, FeatureInfo>();
		// fill in the y map from the query
		txNew.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				jdbcTemplate.query(labelQuery, new RowCallbackHandler() {
					// String currentFeature = null;
					// List<Double> currentFrequencies = new
					// ArrayList<Double>();
					// double currentTotalFreq = 0;

					/**
					 * iterate through results. get per-bin probabilities.
					 * calculate feature's entropy, put in featureEntropyMap
					 */
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String featureName = rs.getString(1).toLowerCase();
						String bin = rs.getString(2).toLowerCase();
						double freq = rs.getDouble(3);
						FeatureInfo info = featureInfoMap.get(featureName);
						if (info == null) {
							info = new FeatureInfo();
							featureInfoMap.put(featureName, info);
						}
						info.getBinToFrequencyMap().put(bin, freq);
						// if (!featureName.equals(currentFeature) ||
						// rs.isLast()) {
						// if (currentFeature != null) {
						// // new feature / end of list - update
						// // make sure the frequency adds up to 1
						// currentFrequencies.add(1d - currentTotalFreq);
						// featureEntropyMap.put(currentFeature,
						// entropy(currentFrequencies));
						// }
						// // reset state
						// currentFrequencies.clear();
						// currentTotalFreq = 0;
						// currentFeature = featureName;
						// }
						// // update current feature
						// currentFrequencies.add(freq);
						// currentTotalFreq += freq;
					}
				});
				return null;
			}
		});
		// compute entropy for each feature
		for (FeatureInfo info : featureInfoMap.values()) {
			double totalFreq = 0;
			for (double freq : info.getBinToFrequencyMap().values())
				totalFreq += freq;
			List<Double> freqs = new ArrayList<Double>();
			freqs.addAll(info.getBinToFrequencyMap().values());
			if (totalFreq < 1d) {
				freqs.add(1d - totalFreq);
			}
			info.setEntropy(entropy(freqs));
		}
		return featureInfoMap;
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
						String cls = rs.getString(3).toLowerCase();
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
			List<Double> classProbs = new ArrayList<Double>(classCountMap
					.size());
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
			if (prob > 0)
				entropy += prob * Math.log(prob) / log2;
		}
		return entropy * -1;
	}

	@SuppressWarnings("static-access")
	public static void main(String args[]) throws ParseException, IOException {
		Options options = new Options();
		options
				.addOption(OptionBuilder
						.withArgName("prop")
						.hasArg()
						.isRequired()
						.withDescription(
								"property file with queries and other parameters.  Expected properties:\ninfogain.name name"
										+ "\nlabel.query query to get Y, i.e. class labels, fold, and class count"
										+ "\nfeature.query query to get X, i.e. feature names, bins, and bin frequencies"
										+ "\nclassfeature.query to get XxY per label, i.e. fold, feature, class, bin, count"
										+ "\nmin.info optional minimum infogain feature must have to be stored")
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
				double minInfo;
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
					minInfo = Double.parseDouble(props.getProperty("min.info",
							"0"));
				} finally {
					if (is != null)
						is.close();
				}
				if (labelQuery != null && name != null && featureQuery != null
						&& classFeatureQuery != null) {
					KernelContextHolder.getApplicationContext().getBean(
							InfoGainEvaluator.class).storeInfoGain(name,
							labelQuery, featureQuery, classFeatureQuery,
							minInfo);
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