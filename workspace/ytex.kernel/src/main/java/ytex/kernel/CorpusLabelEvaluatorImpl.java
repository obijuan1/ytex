package ytex.kernel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.dao.CorpusDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.model.corpus.ConceptLabelStatistic;
import ytex.kernel.model.corpus.CorpusEvaluation;
import ytex.kernel.model.corpus.CorpusLabelEvaluation;

public class CorpusLabelEvaluatorImpl {
	protected SimpleJdbcTemplate simpleJdbcTemplate;
	protected JdbcTemplate jdbcTemplate;
	protected PlatformTransactionManager transactionManager;
	protected TransactionTemplate txNew;
	protected ClassifierEvaluationDao classifierEvaluationDao;
	protected CorpusDao corpusDao;
	protected ConceptDao conceptDao;
	private static final Log log = LogFactory
			.getLog(InfoGainEvaluatorImpl.class);

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
	public void storeInfoGain(String corpusName, String conceptGraphName,
			String conceptSetName, String labelQuery, String featureQuery,
			String classFeatureQuery, Double minInfo, Set<String> xVals,
			Set<String> yVals, String leftoverBin, String mixedBin) {
		CorpusEvaluation eval = initEval(corpusName, conceptGraphName,
				conceptSetName);
		ConceptGraph cg = conceptDao.getConceptGraph(conceptGraphName);
		// load Y - class distributions per fold & label
		Map<String, Map<Integer, Map<String, Set<Integer>>>> labelClassMap = loadY(labelQuery);
		// load X - feature distributions per bin & entropy
		Map<String, FeatureInfo> featureInfoMap = loadFeatureInfoMap(featureQuery);
		// process each label
		for (Map.Entry<String, Map<Integer, Map<String, Set<Integer>>>> labelClass : labelClassMap
				.entrySet()) {
			evalLabel(eval, cg, labelClass.getKey(), labelClass.getValue(),
					featureInfoMap, classFeatureQuery, minInfo == null ? 0
							: minInfo, xVals, yVals, leftoverBin, mixedBin);
		}
	}

	/**
	 * create the corpusEvaluation if it doesn't exist
	 * 
	 * @param corpusName
	 * @param conceptGraphName
	 * @param conceptSetName
	 * @return
	 */
	private CorpusEvaluation initEval(String corpusName,
			String conceptGraphName, String conceptSetName) {
		CorpusEvaluation eval = this.corpusDao.getCorpus(corpusName,
				conceptGraphName, conceptSetName);
		if (eval == null) {
			eval = new CorpusEvaluation();
			eval.setConceptGraphName(conceptGraphName);
			eval.setConceptSetName(conceptSetName);
			eval.setCorpusName(corpusName);
			this.corpusDao.addCorpus(eval);
		}
		return eval;
	}

	/**
	 * joint distribution of concept (x) and class (y). The bins for x and y are
	 * not predetermined - we figure them out as we read in the query. Typical
	 * levels for x are 0/1 (absent/present) and -1/0/1 (negated/not
	 * present/affirmed).
	 * 
	 * @author vijay
	 * 
	 */
	public static class JointDistribution {
		/**
		 * map of class (y) to concept (x) and count.
		 */
		protected SortedMap<String, SortedMap<String, Set<Integer>>> jointDistroTable;
		protected Set<String> xVals;
		protected Set<String> yVals;

		public JointDistribution(Set<String> x, Set<String> y) {
			this.xVals = x;
			this.yVals = y;
			jointDistroTable = new TreeMap<String, SortedMap<String, Set<Integer>>>();
			for (String yVal : y) {
				SortedMap<String, Set<Integer>> yMap = new TreeMap<String, Set<Integer>>();
				jointDistroTable.put(yVal, yMap);
				for (String xVal : x) {
					yMap.put(xVal, new HashSet<Integer>());
				}
			}
		}

		/**
		 * finalize the joint probability table wrt the specified instances. If
		 * we are doing this per fold, then not all instances are going to be in
		 * each fold. Limit to the instances in the specified fold.
		 * <p>
		 * Also, we might not have filled in all the cells. if necessary, add a
		 * 'leftover' cell, fill it in based on the marginal distribution of the
		 * instances wrt classes.
		 * 
		 * @param yMargin
		 *            map of values of y to the instances with that value
		 * @param xLeftover
		 *            the value of x to assign the the leftover instances
		 */
		public JointDistribution complete(Map<String, Set<Integer>> yMargin,
				String xLeftover) {
			JointDistribution foldDistro = new JointDistribution(this.xVals,
					this.yVals);
			for (Map.Entry<String, Set<Integer>> yEntry : yMargin.entrySet()) {
				// iterate over 'rows' i.e. the class names
				String yName = yEntry.getKey();
				Set<Integer> yInst = new HashSet<Integer>(yEntry.getValue());
				// iterate over 'columns' i.e. the values of x
				for (Map.Entry<String, Set<Integer>> xEntry : this.jointDistroTable
						.get(yName).entrySet()) {
					// copy the instances
					Set<Integer> foldXInst = foldDistro.jointDistroTable.get(
							yName).get(xEntry.getKey());
					foldXInst.addAll(xEntry.getValue());
					// keep only the ones that are in this fold
					foldXInst.retainAll(yInst);
					// remove the instances for this value of x from the set of
					// all instances
					yInst.removeAll(foldXInst);
				}
				if (yInst.size() > 0) {
					// add the leftovers to the leftover bin
					foldDistro.jointDistroTable.get(yEntry.getKey())
							.get(xLeftover).addAll(yInst);
				}
			}
			return foldDistro;
		}

		public Set<Integer> getInstances(String x, String y) {
			return jointDistroTable.get(y).get(x);
		}

		/**
		 * add an instance to the joint probability table
		 * 
		 * @param x
		 * @param y
		 * @param instanceId
		 */
		public void addInstance(String x, String y, int instanceId) {
			// add the current row to the bin matrix
			SortedMap<String, Set<Integer>> xMap = jointDistroTable.get(y);
			if (xMap == null) {
				xMap = new TreeMap<String, Set<Integer>>();
				jointDistroTable.put(y, xMap);
			}
			Set<Integer> instanceSet = xMap.get(x);
			if (instanceSet == null) {
				instanceSet = new HashSet<Integer>();
				xMap.put(x, instanceSet);
			}
			instanceSet.add(instanceId);
		}

		/**
		 * merge joint distributions into a single distribution
		 * 
		 * @param jointDistros
		 *            list of joint distribution tables to merge
		 * @param yMargin
		 *            map of y val - instance id. this could be calculated on
		 *            the fly, but we have this information already.
		 * @param xMerge
		 *            the x val that contains everything that doesn't land in
		 *            any of the other bins.
		 * @return
		 */
		public static JointDistribution merge(
				List<JointDistribution> jointDistros,
				Map<String, Set<Integer>> yMargin, String xMerge) {
			Set<String> xVals = jointDistros.get(0).xVals;
			Set<String> yVals = jointDistros.get(0).yVals;
			JointDistribution mergedDistro = new JointDistribution(xVals, yVals);
			for (String y : yVals) {
				// intersect all bins besides the merge bin
				Set<Integer> xMergedInst = mergedDistro.getInstances(xMerge, y);
				// everything comes into the merge bin
				// we take out things that land in other bins
				xMergedInst.addAll(yMargin.get(y));
				// iterate over other bins
				for (String x : xVals) {
					if (!x.equals(xMerge)) {
						Set<Integer> intersectIds = mergedDistro.getInstances(
								x, y);
						boolean bFirstIter = true;
						// iterate over all joint distribution tables
						for (JointDistribution distro : jointDistros) {
							if (bFirstIter) {
								// 1st iter - add all
								intersectIds.addAll(distro.getInstances(x, y));
								bFirstIter = false;
							} else {
								// subsequent iteration - intersect
								intersectIds.retainAll(distro
										.getInstances(x, y));
							}
						}
						// remove from the merge bin
						xMergedInst.removeAll(intersectIds);
					}
				}
			}
			return mergedDistro;
		}

		public double getMutualInformation() {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	/**
	 * iterates through query results and computes infogain
	 * 
	 * @author vijay
	 * 
	 */
	public class JointDistroExtractor implements RowCallbackHandler {
		/**
		 * key - fold
		 * <p/>
		 * value - map of concept id - joint distribution
		 */
		private Map<String, JointDistribution> jointDistroMap;
		private Set<String> xVals;
		private Set<String> yVals;

		public JointDistroExtractor(
				Map<String, JointDistribution> jointDistroMap,
				Set<String> xVals, Set<String> yVals) {
			super();
			this.xVals = xVals;
			this.yVals = yVals;
			this.jointDistroMap = jointDistroMap;
		}

		public void processRow(ResultSet rs) throws SQLException {
			String y = rs.getString(2);
			String conceptId = rs.getString(3);
			String x = rs.getString(4);
			int instanceId = rs.getInt(5);
			JointDistribution distro = jointDistroMap.get(conceptId);
			if (distro == null) {
				distro = new JointDistribution(xVals, yVals);
				jointDistroMap.put(conceptId, distro);
			}
			distro.addInstance(x, y, instanceId);
		}
	}

	private void evalLabel(final CorpusEvaluation corpusEvaluation,
			final ConceptGraph cg, final String label,
			final Map<Integer, Map<String, Set<Integer>>> foldInstanceMap,
			final Map<String, FeatureInfo> featureInfoMap,
			final String featureQuery, double minInfo, final Set<String> xVals,
			final Set<String> yVals, String xLeftover, String xMerge) {
		if (log.isDebugEnabled())
			log.debug("processing label: " + label);
		// load the joint distribution of each fold
		final Map<String, JointDistribution> jointDistroMap = new HashMap<String, JointDistribution>();
		final JointDistroExtractor handler = new JointDistroExtractor(
				jointDistroMap, xVals, yVals);
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
		propagateJointDistribution(jointDistroMap, foldInstanceMap, cg,
				corpusEvaluation, label, xLeftover, xMerge, minInfo);
	}

	/**
	 * <ul>
	 * <li>finalize the joint distribution tables wrt each fold.
	 * <li>propagate across concept graph
	 * <li>save in db
	 * </ul>
	 * 
	 * @param foldJointDistroMap
	 * @param foldInstanceMap
	 * @param cg
	 * @param eval
	 * @param label
	 */
	private void propagateJointDistribution(
			Map<String, JointDistribution> jointDistroMap,
			Map<Integer, Map<String, Set<Integer>>> foldInstanceMap,
			ConceptGraph cg, CorpusEvaluation eval, String label,
			String xLeftover, String xMerge, double minInfo) {
		// iterate over folds
		for (Map.Entry<Integer, Map<String, Set<Integer>>> foldEntry : foldInstanceMap
				.entrySet()) {
			int foldId = foldEntry.getKey();
			Map<String, Set<Integer>> yMargin = foldInstanceMap.get(foldId);
			double yEntropy = calculateFoldEntropy(yMargin);
			// get the joint distribution tables for each fold
			Map<String, JointDistribution> foldJointDistroMap = new HashMap<String, JointDistribution>(
					jointDistroMap.size());
			for (Map.Entry<String, JointDistribution> distro : jointDistroMap
					.entrySet()) {
				foldJointDistroMap.put(distro.getKey(), distro.getValue()
						.complete(yMargin, xLeftover));
			}
			CorpusLabelEvaluation labelEval = initCorpusLabelEval(eval, label,
					foldId);
			propagateJointDistribution(foldJointDistroMap, labelEval, cg,
					yMargin, yEntropy, xMerge, minInfo);
		}

	}

	private void propagateJointDistribution(
			Map<String, JointDistribution> foldJointDistroMap,
			CorpusLabelEvaluation labelEval, ConceptGraph cg,
			Map<String, Set<Integer>> yMargin, double yEntropy,String xMerge, double minInfo) {
		for (String cName : cg.getRoots()) {
			ConcRel cr = cg.getConceptMap().get(cName);
			updateLabelStatistic(cr, foldJointDistroMap, labelEval, yMargin, yEntropy,
					xMerge, minInfo);
		}
	}

	private JointDistribution updateLabelStatistic(ConcRel cr,
			Map<String, JointDistribution> foldJointDistroMap,
			CorpusLabelEvaluation labelEval, Map<String, Set<Integer>> yMargin, double yEntropy,
			String xMerge, double minInfo) {
		List<JointDistribution> distroList = new ArrayList<JointDistribution>(
				cr.children.size() + 1);
		if (foldJointDistroMap.containsKey(cr.getConceptID())) {
			distroList.add(foldJointDistroMap.get(cr.getConceptID()));
		}
		for (ConcRel crc : cr.children) {
			// recurse
			distroList.add(updateLabelStatistic(crc, foldJointDistroMap,
					labelEval, yMargin, yEntropy, xMerge, minInfo));
		}
		JointDistribution mergedDistro = JointDistribution.merge(distroList,
				yMargin, xMerge);
		saveLabelStatistic(cr.getConceptID(), mergedDistro, labelEval, yEntropy, minInfo);
		return mergedDistro;
	}

	private void saveLabelStatistic(String conceptID, JointDistribution distro,
			CorpusLabelEvaluation labelEval, double yEntropy, double minInfo) {
		double mi = distro.getMutualInformation();
		if (mi > minInfo) {
			ConceptLabelStatistic stat = new ConceptLabelStatistic();
			stat.setCorpusLabel(labelEval);
			stat.setMutualInfo(mi);
			stat.setConceptId(conceptID);
			this.corpusDao.addLabelStatistic(stat);
		}
	}

	private CorpusLabelEvaluation initCorpusLabelEval(CorpusEvaluation eval,
			String label, int foldId) {
		CorpusLabelEvaluation labelEval = corpusDao.getCorpusLabelEvaluation(
				eval.getCorpusName(), eval.getConceptGraphName(),
				eval.getConceptSetName(), label, foldId == 0 ? null : foldId);
		if (labelEval == null) {
			labelEval = new CorpusLabelEvaluation();
			labelEval.setCorpus(eval);
			labelEval.setFoldId(foldId == 0 ? null : foldId);
			labelEval.setLabel(label);
			corpusDao.addCorpusLabelEval(labelEval);
		}
		return labelEval;
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
	private Map<String, Map<Integer, Map<String, Set<Integer>>>> loadY(
			final String labelQuery) {
		final Map<String, Map<Integer, Map<String, Set<Integer>>>> y = new HashMap<String, Map<Integer, Map<String, Set<Integer>>>>();
		// // fill in the y map from the query
		// txNew.execute(new TransactionCallback<Object>() {
		//
		// @Override
		// public Object doInTransaction(TransactionStatus txStatus) {
		// jdbcTemplate.query(labelQuery, new RowCallbackHandler() {
		// @Override
		// public void processRow(ResultSet rs) throws SQLException {
		// String label = rs.getString(1);
		// int foldId = rs.getInt(2);
		// String cls = rs.getString(3).toLowerCase();
		// int count = rs.getInt(4);
		// Map<Integer, Map<String, Integer>> foldToClassMap = y
		// .get(label);
		// if (foldToClassMap == null) {
		// foldToClassMap = new HashMap<Integer, Map<String, Integer>>();
		// y.put(label, foldToClassMap);
		// }
		// Map<String, Integer> classToCountMap = foldToClassMap
		// .get(foldId);
		// if (classToCountMap == null) {
		// classToCountMap = new HashMap<String, Integer>();
		// foldToClassMap.put(foldId, classToCountMap);
		// }
		// classToCountMap.put(cls, count);
		// }
		// });
		// return null;
		// }
		// });
		return y;
	}

	/**
	 * 
	 */
	private double calculateFoldEntropy(Map<String, Set<Integer>> classCountMap) {
		int total = 0;
		List<Double> classProbs = new ArrayList<Double>(classCountMap.size());
		// calculate total number of instances in this fold
		for (Set<Integer> instances : classCountMap.values()) {
			total += instances.size();
		}
		// calculate per-class probability in this fold
		for (Set<Integer> instances : classCountMap.values()) {
			classProbs.add((double) instances.size() / (double) total);
		}
		return entropy(classProbs);
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
		options.addOption(OptionBuilder
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
					KernelContextHolder
							.getApplicationContext()
							.getBean(InfoGainEvaluator.class)
							.storeInfoGain(name, labelQuery, featureQuery,
									classFeatureQuery, minInfo);
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
