package ytex.kernel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.dao.CorpusDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.corpus.ConceptLabelChild;
import ytex.kernel.model.corpus.ConceptLabelStatistic;
import ytex.kernel.model.corpus.CorpusEvaluation;
import ytex.kernel.model.corpus.CorpusLabelEvaluation;

/**
 * 
 * @author vijay
 * 
 */
public class CorpusLabelEvaluatorImpl implements CorpusLabelEvaluator {

	/**
	 * joint distribution of concept (x) and class (y). The bins for x and y are
	 * predetermined. Typical levels for x are 0/1 (absent/present) and -1/0/1
	 * (negated/not present/affirmed).
	 * 
	 * @author vijay
	 * 
	 */
	public static class JointDistribution {
		/**
		 * merge joint distributions into a single distribution. For each value
		 * of Y, the cells for each X bin, except for the xMerge bin, are the
		 * intersection of all the instances in each of the corresponding bins.
		 * The xMerge bin gets everything that is leftover.
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

		/**
		 * the entropy of X. Calculated once and returned as needed.
		 */
		protected Double entropyX = null;
		/**
		 * the entropy of X*Y. Calculated once and returned as needed.
		 */
		protected Double entropyXY = null;
		/**
		 * A y*x table where the cells hold the instance ids. We use the
		 * instance ids instead of counts so we can merge the tables.
		 */
		protected SortedMap<String, SortedMap<String, Set<Integer>>> jointDistroTable;
		/**
		 * the possible values of X (e.g. concept)
		 */
		protected Set<String> xVals;
		/**
		 * the possible values of Y (e.g. text)
		 */
		protected Set<String> yVals;

		/**
		 * set up the joint distribution table.
		 * 
		 * @param xVals
		 *            the possible x values (bins)
		 * @param yVals
		 *            the possible y values (bins)
		 */
		public JointDistribution(Set<String> xVals, Set<String> yVals) {
			this.xVals = xVals;
			this.yVals = yVals;
			jointDistroTable = new TreeMap<String, SortedMap<String, Set<Integer>>>();
			for (String yVal : yVals) {
				SortedMap<String, Set<Integer>> yMap = new TreeMap<String, Set<Integer>>();
				jointDistroTable.put(yVal, yMap);
				for (String xVal : xVals) {
					yMap.put(xVal, new HashSet<Integer>());
				}
			}
		}

		public JointDistribution(Set<String> xVals, Set<String> yVals,
				Map<String, Set<Integer>> xMargin,
				Map<String, Set<Integer>> yMargin, String xLeftover) {
			this.xVals = xVals;
			this.yVals = yVals;
			jointDistroTable = new TreeMap<String, SortedMap<String, Set<Integer>>>();
			for (String yVal : yVals) {
				SortedMap<String, Set<Integer>> yMap = new TreeMap<String, Set<Integer>>();
				jointDistroTable.put(yVal, yMap);
				for (String xVal : xVals) {
					yMap.put(xVal, new HashSet<Integer>());
				}
			}
			for (Map.Entry<String, Set<Integer>> yEntry : yMargin.entrySet()) {
				// iterate over 'rows' i.e. the class names
				String yName = yEntry.getKey();
				Set<Integer> yInst = new HashSet<Integer>(yEntry.getValue());
				// iterate over 'columns' i.e. the values of x
				for (Map.Entry<String, Set<Integer>> xEntry : xMargin
						.entrySet()) {
					// copy the instances
					Set<Integer> foldXInst = jointDistroTable.get(yName).get(
							xEntry.getKey());
					foldXInst.addAll(xEntry.getValue());
					// keep only the ones that are in this fold
					foldXInst.retainAll(yInst);
					// remove the instances for this value of x from the set of
					// all instances
					yInst.removeAll(foldXInst);
				}
				if (yInst.size() > 0) {
					// add the leftovers to the leftover bin
					jointDistroTable.get(yEntry.getKey()).get(xLeftover)
							.addAll(yInst);
				}
			}

		}

		// /**
		// * add an instance to the joint probability table
		// *
		// * @param x
		// * @param y
		// * @param instanceId
		// */
		// public void addInstance(String x, String y, int instanceId) {
		// // add the current row to the bin matrix
		// SortedMap<String, Set<Integer>> xMap = jointDistroTable.get(y);
		// if (xMap == null) {
		// xMap = new TreeMap<String, Set<Integer>>();
		// jointDistroTable.put(y, xMap);
		// }
		// Set<Integer> instanceSet = xMap.get(x);
		// if (instanceSet == null) {
		// instanceSet = new HashSet<Integer>();
		// xMap.put(x, instanceSet);
		// }
		// instanceSet.add(instanceId);
		// }

		// /**
		// * finalize the joint probability table wrt the specified instances.
		// If
		// * we are doing this per fold, then not all instances are going to be
		// in
		// * each fold. Limit to the instances in the specified fold.
		// * <p>
		// * Also, we might not have filled in all the cells. if necessary, put
		// * instances in the 'leftover' cell, fill it in based on the marginal
		// * distribution of the instances wrt classes.
		// *
		// * @param yMargin
		// * map of values of y to the instances with that value
		// * @param xLeftover
		// * the value of x to assign the the leftover instances
		// */
		// public JointDistribution complete(Map<String, Set<Integer>> xMargin,
		// Map<String, Set<Integer>> yMargin, String xLeftover) {
		// JointDistribution foldDistro = new JointDistribution(this.xVals,
		// this.yVals);
		// for (Map.Entry<String, Set<Integer>> yEntry : yMargin.entrySet()) {
		// // iterate over 'rows' i.e. the class names
		// String yName = yEntry.getKey();
		// Set<Integer> yInst = new HashSet<Integer>(yEntry.getValue());
		// // iterate over 'columns' i.e. the values of x
		// for (Map.Entry<String, Set<Integer>> xEntry : this.jointDistroTable
		// .get(yName).entrySet()) {
		// // copy the instances
		// Set<Integer> foldXInst = foldDistro.jointDistroTable.get(
		// yName).get(xEntry.getKey());
		// foldXInst.addAll(xEntry.getValue());
		// // keep only the ones that are in this fold
		// foldXInst.retainAll(yInst);
		// // remove the instances for this value of x from the set of
		// // all instances
		// yInst.removeAll(foldXInst);
		// }
		// if (yInst.size() > 0) {
		// // add the leftovers to the leftover bin
		// foldDistro.jointDistroTable.get(yEntry.getKey())
		// .get(xLeftover).addAll(yInst);
		// }
		// }
		// return foldDistro;
		// }

		public double getEntropyX() {
			double probs[] = new double[xVals.size()];
			Arrays.fill(probs, 0d);
			if (entropyX == null) {
				double nTotal = 0;
				for (Map<String, Set<Integer>> xInstance : this.jointDistroTable
						.values()) {
					int i = 0;
					for (Set<Integer> instances : xInstance.values()) {
						double nCell = (double) instances.size();
						nTotal += nCell;
						probs[i] += nCell;
						i++;
					}
				}
				for (int i = 0; i < probs.length; i++)
					probs[i] /= nTotal;
				entropyX = entropy(probs);
			}
			return entropyX;
		}

		public double getEntropyXY() {
			double probs[] = new double[xVals.size() * yVals.size()];
			Arrays.fill(probs, 0d);
			if (entropyXY == null) {
				double nTotal = 0;
				int i = 0;
				for (Map<String, Set<Integer>> xInstance : this.jointDistroTable
						.values()) {
					for (Set<Integer> instances : xInstance.values()) {
						probs[i] = (double) instances.size();
						nTotal += probs[i];
						i++;
					}
				}
				for (int j = 0; j < probs.length; j++)
					probs[j] /= nTotal;
				entropyXY = entropy(probs);
			}
			return entropyXY;
		}

		public Set<Integer> getInstances(String x, String y) {
			return jointDistroTable.get(y).get(x);
		}

		public double getMutualInformation(double entropyY) {
			return entropyY + this.getEntropyX() - this.getEntropyXY();
		}

		/**
		 * print out joint distribution table
		 */
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(this.getClass().getCanonicalName());
			b.append(" [jointDistro=(");
			Iterator<Entry<String, SortedMap<String, Set<Integer>>>> yIter = this.jointDistroTable
					.entrySet().iterator();
			while (yIter.hasNext()) {
				Entry<String, SortedMap<String, Set<Integer>>> yEntry = yIter
						.next();
				Iterator<Entry<String, Set<Integer>>> xIter = yEntry.getValue()
						.entrySet().iterator();
				while (xIter.hasNext()) {
					Entry<String, Set<Integer>> xEntry = xIter.next();
					b.append(xEntry.getValue().size());
					if (xIter.hasNext())
						b.append(", ");
				}
				if (yIter.hasNext())
					b.append("| ");
			}
			b.append(")]");
			return b.toString();
		}
	}

	/**
	 * fill in map of Concept Id - bin - instance ids
	 * 
	 * @author vijay
	 * 
	 */
	public class ConceptInstanceMapExtractor implements RowCallbackHandler {
		Map<String, Map<String, Set<Integer>>> conceptInstanceMap;
		ConceptGraph cg;

		ConceptInstanceMapExtractor(
				Map<String, Map<String, Set<Integer>>> conceptInstanceMap,
				ConceptGraph cg) {
			this.cg = cg;
			this.conceptInstanceMap = conceptInstanceMap;
		}

		public void processRow(ResultSet rs) throws SQLException {
			String conceptId = rs.getString(1);
			int instanceId = rs.getInt(2);
			String x = rs.getString(3);
			// limit to concepts from our graph
			ConcRel cr = cg.getConceptMap().get(conceptId);
			if (cr != null) {
				Map<String, Set<Integer>> binInstanceMap = conceptInstanceMap
						.get(cr.getConceptID());
				if (binInstanceMap == null) {
					// use the conceptId from the concept to save memory
					binInstanceMap = new HashMap<String, Set<Integer>>(2);
					conceptInstanceMap.put(cr.getConceptID(), binInstanceMap);
				}
				Set<Integer> instanceIds = binInstanceMap.get(x);
				if (instanceIds == null) {
					instanceIds = new HashSet<Integer>();
					binInstanceMap.put(x, instanceIds);
				}
				instanceIds.add(instanceId);
			}
		}

	}

	// /**
	// * iterates through query results and computes infogain
	// *
	// * @author vijay
	// *
	// */
	// public class JointDistroExtractor implements RowCallbackHandler {
	// /**
	// * key - fold
	// * <p/>
	// * value - map of concept id - joint distribution
	// */
	// private Map<String, JointDistribution> jointDistroMap;
	// private Set<String> xVals;
	// private Set<String> yVals;
	// private Map<Integer, String> instanceClassMap;
	//
	// public JointDistroExtractor(
	// Map<String, JointDistribution> jointDistroMap,
	// Set<String> xVals, Set<String> yVals,
	// Map<Integer, String> instanceClassMap) {
	// super();
	// this.xVals = xVals;
	// this.yVals = yVals;
	// this.jointDistroMap = jointDistroMap;
	// this.instanceClassMap = instanceClassMap;
	// }
	//
	// public void processRow(ResultSet rs) throws SQLException {
	// int instanceId = rs.getInt(1);
	// String conceptId = rs.getString(2);
	// String x = rs.getString(3);
	// String y = instanceClassMap.get(instanceId);
	// JointDistribution distro = jointDistroMap.get(conceptId);
	// if (distro == null) {
	// distro = new JointDistribution(xVals, yVals);
	// jointDistroMap.put(conceptId, distro);
	// }
	// distro.addInstance(x, y, instanceId);
	// }
	// }

	private static final Log log = LogFactory
			.getLog(CorpusLabelEvaluatorImpl.class);

	protected static double entropy(double[] classProbs) {
		double entropy = 0;
		double log2 = Math.log(2);
		for (double prob : classProbs) {
			if (prob > 0)
				entropy += prob * Math.log(prob) / log2;
		}
		return entropy * -1;
	}

	/**
	 * calculate entropy from a list/array of probabilities
	 * 
	 * @param classProbs
	 * @return
	 */
	protected static double entropy(Iterable<Double> classProbs) {
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
						"property file with queries and other parameters. todo desc")
				.create("prop"));
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);
			if (!KernelContextHolder.getApplicationContext()
					.getBean(CorpusLabelEvaluator.class)
					.evaluateCorpus(line.getOptionValue("prop"))) {
				printHelp(options);
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

	protected ClassifierEvaluationDao classifierEvaluationDao;

	protected ConceptDao conceptDao;

	protected CorpusDao corpusDao;

	protected JdbcTemplate jdbcTemplate;

	protected NamedParameterJdbcTemplate namedParamJdbcTemplate;

	protected KernelUtil kernelUtil;

	protected PlatformTransactionManager transactionManager;

	protected TransactionTemplate txNew;

	private JointDistribution calcMergedJointDistribution(
			Map<String, JointDistribution> conceptJointDistroMap,
			Map<String, Integer> conceptDistMap, ConcRel cr,
			Map<String, JointDistribution> rawJointDistroMap,
			CorpusLabelEvaluation labelEval, Map<String, Set<Integer>> yMargin,
			String xMerge, double minInfo, List<String> path) {
		if (conceptJointDistroMap.containsKey(cr.getConceptID())) {
			return conceptJointDistroMap.get(cr.getConceptID());
		} else {
			List<JointDistribution> distroList = new ArrayList<JointDistribution>(
					cr.children.size() + 1);
			int distance = -1;
			// if this concept is in the raw joint distro map, add it to the
			// list of joint distributions to merge
			if (rawJointDistroMap.containsKey(cr.getConceptID())) {
				JointDistribution rawJointDistro = rawJointDistroMap.get(cr
						.getConceptID());
				distroList.add(rawJointDistro);
				distance = 0;
			}
			// get the joint distributions of children
			for (ConcRel crc : cr.getChildren()) {
				List<String> pathChild = new ArrayList<String>(path.size() + 1);
				pathChild.addAll(path);
				pathChild.add(crc.getConceptID());
				// recurse - get joint distribution of children
				JointDistribution jdChild = calcMergedJointDistribution(
						conceptJointDistroMap, conceptDistMap, crc,
						rawJointDistroMap, labelEval, yMargin, xMerge, minInfo,
						pathChild);
				if (jdChild != null) {
					distroList.add(jdChild);
					if (distance > 0) {
						// look at children's distance from raw data, add 1
						int distChild = conceptDistMap.get(crc.getConceptID());
						if ((distChild + 1) < distance) {
							distance = distChild + 1;
						}
					}
				}
			}
			// merge the joint distributions
			JointDistribution mergedDistro;
			if (distroList.size() > 0) {
				if (distroList.size() == 1) {
					// only one joint distro - trivial merge
					mergedDistro = distroList.get(0);
				} else {
					// multiple joint distros - merge them into a new one
					mergedDistro = JointDistribution.merge(distroList, yMargin,
							xMerge);
				}
				// if (log.isDebugEnabled()) {
				// log.debug("path = " + path + ", distroList = " + distroList
				// + ", distro = " + mergedDistro);
				// }
			} else {
				// no joint distros to merge - null
				mergedDistro = null;
			}
			// save this in the map
			conceptJointDistroMap.put(cr.getConceptID(), mergedDistro);
			if (distance > -1)
				conceptDistMap.put(cr.getConceptID(), distance);
			return mergedDistro;
		}
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
	 * finalize the joint distribution tables wrt a fold.
	 * 
	 * @param jointDistroMap
	 * @param yMargin
	 * @param yVals
	 * @param xVals
	 * @param xLeftover
	 */
	private Map<String, JointDistribution> completeJointDistroForFold(
			Map<String, Map<String, Set<Integer>>> conceptInstanceMap,
			Map<String, Set<Integer>> yMargin, Set<String> xVals,
			Set<String> yVals, String xLeftover) {
		//
		Map<String, JointDistribution> foldJointDistroMap = new HashMap<String, JointDistribution>(
				conceptInstanceMap.size());
		for (Map.Entry<String, Map<String, Set<Integer>>> conceptInstance : conceptInstanceMap
				.entrySet()) {
			foldJointDistroMap.put(
					conceptInstance.getKey(),
					new JointDistribution(xVals, yVals, conceptInstance
							.getValue(), yMargin, xLeftover));
		}
		return foldJointDistroMap;
	}

	@Override
	public boolean evaluateCorpus(String propFile) throws IOException {
		Properties props = FileUtil.loadProperties(propFile, true);
		String corpusName = props.getProperty("ytex.corpusName");
		String conceptGraphName = props.getProperty("ytex.conceptGraphName");
		String conceptSetName = props.getProperty("ytex.conceptSetName");
		String labelQuery = props.getProperty("instanceClassQuery");
		String classFeatureQuery = props
				.getProperty("ytex.conceptInstanceQuery");
		double minInfo = Double.parseDouble(props.getProperty("min.info",
				"1e-4"));
		String xValStr = props.getProperty("ytex.xVals", "0,1");
		Set<String> xVals = new HashSet<String>();
		xVals.addAll(Arrays.asList(xValStr.split(",")));
		String xLeftover = props.getProperty("ytex.xLeftover", "0");
		String xMerge = props.getProperty("ytex.xMerge", "1");
		Double parentConceptMutualInfoThreshold = FileUtil.getDoubleProperty(
				props, "ytex.parentConceptMutualInfoThreshold", null);
		Integer parentConceptThreshold = parentConceptMutualInfoThreshold == null ? FileUtil
				.getIntegerProperty(props,
						"ytex.parentConceptMutualInfoThreshold", 100) : null;
		if (corpusName != null && conceptGraphName != null
				&& labelQuery != null && classFeatureQuery != null) {
			this.evaluateCorpus(corpusName, conceptGraphName, conceptSetName,
					labelQuery, classFeatureQuery, minInfo, xVals, xLeftover,
					xMerge, parentConceptThreshold,
					parentConceptMutualInfoThreshold);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.CorpusLabelEvaluator#evaluateCorpus(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.Double, java.util.Set, java.lang.String, java.lang.String)
	 */
	@Override
	public void evaluateCorpus(String corpusName, String conceptGraphName,
			String conceptSetName, String labelQuery, String classFeatureQuery,
			Double minInfo, Set<String> xVals, String xLeftover, String xMerge,
			Integer parentConceptThreshold,
			Double parentConceptMutualInfoThreshold) {
		CorpusEvaluation eval = initEval(corpusName, conceptGraphName,
				conceptSetName);
		ConceptGraph cg = conceptDao.getConceptGraph(conceptGraphName);
		InstanceData instanceData = this.kernelUtil.loadInstances(labelQuery);
		for (String label : instanceData.getLabelToInstanceMap().keySet()) {
			evaluateCorpusLabel(classFeatureQuery, minInfo, xVals, xLeftover,
					xMerge, eval, cg, instanceData, label,
					parentConceptThreshold, parentConceptMutualInfoThreshold);
		}
	}

	/**
	 * evaluate corpus on label
	 * 
	 * @param classFeatureQuery
	 * @param minInfo
	 * @param xVals
	 * @param xLeftover
	 * @param xMerge
	 * @param eval
	 * @param cg
	 * @param instanceData
	 * @param label
	 * @param parentConceptThreshold
	 * @param parentConceptMutualInfoThreshold
	 */
	private void evaluateCorpusLabel(String classFeatureQuery, Double minInfo,
			Set<String> xVals, String xLeftover, String xMerge,
			CorpusEvaluation eval, ConceptGraph cg, InstanceData instanceData,
			String label, Integer parentConceptThreshold,
			Double parentConceptMutualInfoThreshold) {
		if (log.isDebugEnabled())
			log.debug("evaluateCorpusLabel() label = " + label);
		Map<String, Map<String, Set<Integer>>> conceptInstanceMap = loadConceptInstanceMap(
				classFeatureQuery, cg, label);
		for (int run : instanceData.getLabelToInstanceMap().get(label).keySet()) {
			for (int fold : instanceData.getLabelToInstanceMap().get(label)
					.get(run).keySet()) {
				evaluateCorpusFold(minInfo, xVals, xLeftover, xMerge, eval, cg,
						instanceData, label, conceptInstanceMap, run, fold,
						parentConceptThreshold,
						parentConceptMutualInfoThreshold);
			}
		}
	}

	private void evaluateCorpusFold(Double minInfo, Set<String> xVals,
			String xLeftover, String xMerge, CorpusEvaluation eval,
			ConceptGraph cg, InstanceData instanceData, String label,
			Map<String, Map<String, Set<Integer>>> conceptInstanceMap, int run,
			int fold, Integer parentConceptThreshold,
			Double parentConceptMutualInfoThreshold) {
		if (log.isDebugEnabled())
			log.debug("evaluateCorpusFold() label = " + label + ", fold = "
					+ fold + ", run = " + run);
		// evaluate for the specified fold training set
		// construct map of class - [instance ids]
		Map<String, Set<Integer>> yMargin = getFoldYMargin(instanceData, label,
				run, fold);
		// get the joint distribution of concepts and instances
		Map<String, JointDistribution> rawJointDistro = this
				.completeJointDistroForFold(conceptInstanceMap, yMargin, xVals,
						instanceData.getLabelToClassMap().get(label), xLeftover);
		// initialize the object we'll be saving all this in
		CorpusLabelEvaluation labelEval = this.initCorpusLabelEval(eval, label,
				run, fold);
		// propagate across graph and save
		propagateJointDistribution(rawJointDistro, labelEval, cg, yMargin,
				xMerge, minInfo);
		// store children of top concepts
		storeChildConcepts(labelEval, parentConceptThreshold,
				parentConceptMutualInfoThreshold, cg);
	}

	/**
	 * save the children of the 'top' parent concepts.
	 * 
	 * @param labelEval
	 * @param parentConceptThreshold
	 * @param parentConceptMutualInfoThreshold
	 * @param cg
	 */
	private void storeChildConcepts(CorpusLabelEvaluation labelEval,
			Integer parentConceptThreshold,
			Double parentConceptMutualInfoThreshold, ConceptGraph cg) {
		// get the top parent concepts - use either top N, or those with a
		// cutoff greater than the specified threshold
		List<ConceptLabelStatistic> listConceptStat = parentConceptThreshold != null ? this.corpusDao
				.getTopCorpusLabelStat(labelEval, parentConceptThreshold)
				: this.corpusDao.getThresholdCorpusLabelStat(labelEval,
						parentConceptMutualInfoThreshold);
		// map of concept id to children and the 'best' statistic
		Map<String, ConceptLabelChild> mapChildConcept = new HashMap<String, ConceptLabelChild>();
		// get all the children of the parent concepts
		for (ConceptLabelStatistic parentConcept : listConceptStat) {
			updateChildren(parentConcept, mapChildConcept, labelEval, cg);
		}
		// save the concepts
		this.corpusDao.saveConceptLabelChildren(mapChildConcept.values());
	}

	/**
	 * add the children of parentConcept to mapChildConcept. Assign the child
	 * the best mutual information value of the parent.
	 * 
	 * @param parentConcept
	 * @param mapChildConcept
	 * @param labelEval
	 * @param cg
	 */
	private void updateChildren(ConceptLabelStatistic parentConcept,
			Map<String, ConceptLabelChild> mapChildConcept,
			CorpusLabelEvaluation labelEval, ConceptGraph cg) {
		ConcRel cr = cg.getConceptMap().get(parentConcept.getConceptId());
		Set<String> childConcepts = new HashSet<String>();
		addSubtree(childConcepts, cr);
		for (String childConceptId : childConcepts) {
			ConceptLabelChild chd = mapChildConcept.get(childConceptId);
			if (chd == null) {
				chd = new ConceptLabelChild();
				mapChildConcept.put(childConceptId, chd);
				chd.setConceptId(childConceptId);
				chd.setCorpusLabel(labelEval);
			}
			if (chd.getMutualInfo() < parentConcept.getMutualInfo()) {
				chd.setMutualInfo(parentConcept.getMutualInfo());
			}
		}
	}

	/**
	 * recursively add children of cr to childConcepts
	 * 
	 * @param childConcepts
	 * @param cr
	 */
	private void addSubtree(Set<String> childConcepts, ConcRel cr) {
		childConcepts.add(cr.getConceptID());
		for (ConcRel crc : cr.getChildren()) {
			addSubtree(childConcepts, crc);
		}
	}

	/**
	 * load the map of concept - instances
	 * 
	 * @param classFeatureQuery
	 * @param cg
	 * @param label
	 * @return
	 */
	private Map<String, Map<String, Set<Integer>>> loadConceptInstanceMap(
			String classFeatureQuery, ConceptGraph cg, String label) {
		Map<String, Map<String, Set<Integer>>> conceptInstanceMap = new HashMap<String, Map<String, Set<Integer>>>();
		Map<String, Object> args = new HashMap<String, Object>(1);
		if (label != null && label.length() > 0) {
			args.put("label", label);
		}
		ConceptInstanceMapExtractor ex = new ConceptInstanceMapExtractor(
				conceptInstanceMap, cg);
		this.namedParamJdbcTemplate.query(classFeatureQuery, args, ex);
		return conceptInstanceMap;
	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public ConceptDao getConceptDao() {
		return conceptDao;
	}

	public CorpusDao getCorpusDao() {
		return corpusDao;
	}

	public DataSource getDataSource(DataSource ds) {
		return this.jdbcTemplate.getDataSource();
	}

	private Map<String, Set<Integer>> getFoldYMargin(InstanceData instanceData,
			String label, int run, int fold) {
		Map<Integer, String> instanceClassMap = instanceData
				.getLabelToInstanceMap().get(label).get(run).get(fold)
				.get(true);
		Map<String, Set<Integer>> yMargin = new HashMap<String, Set<Integer>>();
		for (Map.Entry<Integer, String> instanceClass : instanceClassMap
				.entrySet()) {
			Set<Integer> instanceIds = yMargin.get(instanceClass.getValue());
			if (instanceIds == null) {
				instanceIds = new HashSet<Integer>();
				yMargin.put(instanceClass.getValue(), instanceIds);
			}
			instanceIds.add(instanceClass.getKey());
		}
		return yMargin;
	}

	public KernelUtil getKernelUtil() {
		return kernelUtil;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	private CorpusLabelEvaluation initCorpusLabelEval(CorpusEvaluation eval,
			String label, int run, int fold) {
		// figure out fold id
		Integer foldId = null;
		if (run > 0 && fold > 0) {
			CrossValidationFold cvFold = this.classifierEvaluationDao
					.getCrossValidationFold(eval.getCorpusName(), label, run,
							fold);
			if (cvFold != null) {
				foldId = cvFold.getCrossValidationFoldId();
			} else {
				log.warn("could not find cv fold, name=" + eval.getCorpusName()
						+ ", run=" + run + ", fold=" + fold);
			}
		}
		// see if the labelEval is already there
		CorpusLabelEvaluation labelEval = corpusDao.getCorpusLabelEvaluation(
				eval.getCorpusName(), eval.getConceptGraphName(),
				eval.getConceptSetName(), label, foldId);
		if (labelEval == null) {
			// not there - add it
			labelEval = new CorpusLabelEvaluation();
			labelEval.setCorpus(eval);
			labelEval.setFoldId(foldId);
			labelEval.setLabel(label);
			corpusDao.addCorpusLabelEval(labelEval);
		}
		return labelEval;
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

	// /**
	// * load the raw joint distribution of X & Y for the specified label using
	// * the specified feature query. Do this per-label to enable
	// parallelization
	// * and to avoid killing the DB.
	// *
	// * @param label
	// * @param featureQuery
	// * @param xVals
	// * @param yVals
	// * @return
	// */
	// private Map<String, JointDistribution> loadJointDistribution(
	// final String label, final String featureQuery,
	// final Set<String> xVals, final Set<String> yVals,
	// final Map<Integer, String> instanceClassMap) {
	// final Map<String, JointDistribution> jointDistroMap = new HashMap<String,
	// JointDistribution>();
	// final JointDistroExtractor handler = new JointDistroExtractor(
	// jointDistroMap, xVals, yVals, instanceClassMap);
	// txNew.execute(new TransactionCallback<Object>() {
	//
	// @Override
	// public Object doInTransaction(TransactionStatus txStatus) {
	// jdbcTemplate.query(new PreparedStatementCreator() {
	//
	// @Override
	// public PreparedStatement createPreparedStatement(
	// Connection conn) throws SQLException {
	// PreparedStatement ps = conn.prepareStatement(
	// featureQuery, ResultSet.TYPE_FORWARD_ONLY,
	// ResultSet.CONCUR_READ_ONLY);
	// ps.setString(1, label);
	// return ps;
	// }
	//
	// }, handler);
	// return null;
	// }
	// });
	// return jointDistroMap;
	// }

	/**
	 * 'complete' the joint distribution tables wrt a fold (yMargin). propagate
	 * the joint distribution of all concepts recursively.
	 * 
	 * @param rawJointDistroMap
	 * @param labelEval
	 * @param cg
	 * @param yMargin
	 * @param xMerge
	 * @param minInfo
	 */
	private void propagateJointDistribution(
			Map<String, JointDistribution> rawJointDistroMap,
			CorpusLabelEvaluation labelEval, ConceptGraph cg,
			Map<String, Set<Integer>> yMargin, String xMerge, double minInfo) {
		// get the entropy of Y for this fold
		double yEntropy = this.calculateFoldEntropy(yMargin);
		// allocate a map to hold the results of the propagation across the
		// concept graph
		Map<String, JointDistribution> conceptJointDistroMap = new HashMap<String, JointDistribution>(
				cg.getConceptMap().size());
		Map<String, Integer> conceptDistMap = new HashMap<String, Integer>();
		for (String cName : cg.getRoots()) {
			ConcRel cr = cg.getConceptMap().get(cName);
			// recurse
			calcMergedJointDistribution(conceptJointDistroMap, conceptDistMap,
					cr, rawJointDistroMap, labelEval, yMargin, xMerge, minInfo,
					Arrays.asList(new String[] { cr.getConceptID() }));
		}
		// save the results
		for (Map.Entry<String, JointDistribution> conceptJointDistro : conceptJointDistroMap
				.entrySet()) {
			String conceptID = conceptJointDistro.getKey();
			JointDistribution distroMerged = conceptJointDistro.getValue();
			if (distroMerged != null)
				saveLabelStatistic(conceptID, distroMerged,
						rawJointDistroMap.get(conceptID), labelEval, yEntropy,
						minInfo, conceptDistMap.get(conceptID));
		}
	}

	private void saveLabelStatistic(String conceptID,
			JointDistribution distroMerged, JointDistribution distroRaw,
			CorpusLabelEvaluation labelEval, double yEntropy, double minInfo,
			int distance) {
		double miMerged = distroMerged.getMutualInformation(yEntropy);
		double miRaw = distroRaw != null ? distroRaw
				.getMutualInformation(yEntropy) : 0;
		if (miMerged > minInfo || miRaw > minInfo) {
			ConceptLabelStatistic stat = new ConceptLabelStatistic();
			stat.setCorpusLabel(labelEval);
			stat.setMutualInfo(miMerged);
			if (distroRaw != null)
				stat.setMutualInfoRaw(miRaw);
			stat.setConceptId(conceptID);
			stat.setDistance(distance);
			this.corpusDao.addLabelStatistic(stat);
		}
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public void setConceptDao(ConceptDao conceptDao) {
		this.conceptDao = conceptDao;
	}

	public void setCorpusDao(CorpusDao corpusDao) {
		this.corpusDao = corpusDao;
	}

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
		this.namedParamJdbcTemplate = new NamedParameterJdbcTemplate(ds);
	}

	public void setKernelUtil(KernelUtil kernelUtil) {
		this.kernelUtil = kernelUtil;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		txNew = new TransactionTemplate(transactionManager);
		txNew.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
	}

}
