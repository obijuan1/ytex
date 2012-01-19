package ytex.kernel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.metric.LCHMetric;
import ytex.kernel.metric.LinMetric;
import ytex.kernel.metric.SimilarityInfo;
import ytex.kernel.metric.SimilarityMetric;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.model.FeatureRank;

/**
 * compute concept similarity
 * 
 * @author vijay
 * 
 */
public class ConceptSimilarityServiceImpl implements ConceptSimilarityService {
	private static final Log log = LogFactory
			.getLog(ConceptSimilarityServiceImpl.class);

	private static ThreadLocal<NumberFormat> tlnf = new ThreadLocal<NumberFormat>() {

		@Override
		protected NumberFormat initialValue() {
			return new DecimalFormat("#.######");
		}

	};

	@SuppressWarnings("static-access")
	public static void main(String args[]) throws IOException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("concepts")
				.hasArg()
				.withDescription(
						"concept pairs or a file containing concept pairs.  To specify pairs on command line, separate concepts by comma, concept pairs by semicolon.  For file, separate concepts by comma, each concept pair on a new line.")
				.isRequired(true).create("concepts"));
		options.addOption(OptionBuilder
				.withArgName("metrics")
				.hasArg()
				.withDescription(
						"comma-separated list of metrics.  Valid metrics: "
								+ Arrays.asList(SimilarityMetricEnum.values()))
				.isRequired(true).create("metrics"));
		options.addOption(OptionBuilder
				.withArgName("out")
				.hasArg()
				.withDescription(
						"file to write oputput to.  if not specified, output sent to stdout.")
				.create("out"));
		options.addOption(OptionBuilder.withArgName("lcs")
				.withDescription("output lcs and path for each concept pair")
				.create("lcs"));
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);
			String concepts = line.getOptionValue("concepts");
			String metrics = line.getOptionValue("metrics");
			String out = line.getOptionValue("out");
			boolean lcs = line.hasOption("lcs");
			PrintStream os = null;
			try {
				if (out != null) {
					os = new PrintStream(new BufferedOutputStream(
							new FileOutputStream(out)));
				} else {
					os = System.out;
				}
				List<ConceptPair> conceptPairs = parseConcepts(concepts);
				Set<SimilarityMetricEnum> metricSet = parseMetrics(metrics);
				ConceptSimilarityService simSvc = SimSvcContextHolder
						.getApplicationContext().getBean(
								ConceptSimilarityService.class);
				List<SimilarityInfo> simInfos = lcs ? new ArrayList<SimilarityInfo>(
						conceptPairs.size()) : null;
				List<Map<SimilarityMetricEnum, Double>> conceptSimMap = simSvc
						.similarity(conceptPairs, metricSet, null, simInfos);
				printSimilarities(conceptPairs, conceptSimMap, metricSet,
						simInfos, os);
			} finally {
				if (out != null) {
					try {
						os.close();
					} catch (Exception e) {
					}
				}
			}
		} catch (ParseException pe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					"java " + ConceptSimilarityServiceImpl.class.getName()
							+ " get concept similiarity", options);
		}
	}

	private static List<ConceptPair> parseConcepts(String concepts)
			throws IOException {
		BufferedReader r = null;
		try {
			List<ConceptPair> conceptPairs = new ArrayList<ConceptPair>();
			File f = new File(concepts);
			if (f.exists()) {
				r = new BufferedReader(new FileReader(f));
			} else {
				r = new BufferedReader(new StringReader(concepts));
			}
			String line = null;
			while ((line = r.readLine()) != null) {
				// for command line, split pairs by semicolon
				String lines[] = line.split(";");
				for (String subline : lines) {
					String pair[] = subline.split(",|\\t");
					if (pair.length != 2) {
						System.err
								.println("cannot parse concept pair: " + subline);
					} else {
						conceptPairs.add(new ConceptPair(pair[0], pair[1]));
					}
				}
			}
			return conceptPairs;
		} finally {
			if (r != null)
				r.close();
		}
	}

	private static Set<SimilarityMetricEnum> parseMetrics(String metrics) {
		String ms[] = metrics.split(",");
		Set<SimilarityMetricEnum> metricSet = new HashSet<SimilarityMetricEnum>();
		for (String metric : ms) {
			SimilarityMetricEnum m = SimilarityMetricEnum.valueOf(metric);
			if (m == null)
				System.err.println("invalid metric: " + ms);
			else
				metricSet.add(m);
		}
		return metricSet;
	}

	private static void printSimilarities(List<ConceptPair> conceptPairs,
			List<Map<SimilarityMetricEnum, Double>> conceptSims,
			Set<SimilarityMetricEnum> metrics, List<SimilarityInfo> simInfos,
			PrintStream os) {
		// print header
		os.print("Concept 1\tConcept 2");
		for (SimilarityMetricEnum metric : metrics) {
			os.print("\t");
			os.print(metric);
		}
		if (simInfos != null) {
			os.print("\tlcs(s)\tcorpus lcs\tintrinsic lcs\tpaths");
		}
		os.println();
		// print content
		for (int i = 0; i < conceptPairs.size(); i++) {
			ConceptPair p = conceptPairs.get(i);
			Map<SimilarityMetricEnum, Double> s = conceptSims.get(i);
			os.print(p.getConcept1());
			os.print("\t");
			os.print(p.getConcept2());
			for (SimilarityMetricEnum metric : metrics) {
				os.print("\t");
				Double sim = s.get(metric);
				if (sim != null)
					os.print(String.format("%6f", sim));
				else
					os.print(0d);
			}
			if (simInfos != null) {
				SimilarityInfo simInfo = simInfos.get(i);
				os.print("\t");
				Iterator<String> lcsIter = simInfo.getLcses().iterator();
				while (lcsIter.hasNext()) {
					os.print(lcsIter.next());
					if (lcsIter.hasNext())
						os.print(',');
				}
				os.print("\t");
				os.print(simInfo.getCorpusLcs() == null ? "" : simInfo
						.getCorpusLcs());
				os.print("\t");
				os.print(simInfo.getIntrinsicLcs() == null ? "" : simInfo
						.getIntrinsicLcs());
				os.print("\t");
				os.print(formatPaths(simInfo.getLcsPathMap()));
			}
			os.println();
		}
	}

	private static String formatPaths(Map<String, List<List<String>>> lcsPathMap) {
		StringBuilder b = new StringBuilder();
		Iterator<Map.Entry<String, List<List<String>>>> lcsPathIter = lcsPathMap
				.entrySet().iterator();
		while (lcsPathIter.hasNext()) {
			Map.Entry<String, List<List<String>>> lcsPath = lcsPathIter.next();
			String lcs = lcsPath.getKey();
			b.append(lcs);
			b.append("=");
			if (lcsPath.getValue().size() > 0
					&& lcsPath.getValue().get(0).size() > 0) {
				formatPath(b, "->", lcsPath.getValue().get(0).iterator());
				b.append("->*").append(lcs);
			}
			if (lcsPath.getValue().size() == 2
					&& lcsPath.getValue().get(1).size() > 0) {
				b.append("*<-");
				formatPath(b, "<-", lcsPath.getValue().get(1).iterator());
			}
			if (lcsPathIter.hasNext())
				b.append("|");
		}
		return b.toString();
	}

	private static void formatPath(StringBuilder b, String link,
			Iterator<String> pathIter) {
		while (pathIter.hasNext()) {
			b.append(pathIter.next());
			if (pathIter.hasNext()) {
				b.append(link);
			}
		}
	}

	private CacheManager cacheManager;

	private ConceptGraph cg = null;

	private ClassifierEvaluationDao classifierEvaluationDao;

	private ConceptDao conceptDao;

	private String conceptGraphName;

	private String conceptSetName;

	/**
	 * information concept cache
	 */
	private Map<String, Double> corpusICMap = null;

	private String corpusName;
	private Map<String, BitSet> cuiTuiMap;
	/**
	 * 
	 */
	private Map<String, Double> intrinsicICMap = null;
	/**
	 * cache to hold lcs's
	 */
	private Cache lcsCache;

	private String lcsImputedType = ImputedFeatureEvaluator.MeasureType.INFOGAIN
			.getName();

	private Map<SimilarityMetricEnum, SimilarityMetric> similarityMetricMap = null;

	private PlatformTransactionManager transactionManager;

	private List<String> tuiList;

	private void addCuiTuiToMap(Map<String, Set<String>> cuiTuiMap,
			Map<String, String> tuiMap, String cui, String tui) {
		// get 'the' tui string
		if (tuiMap.containsKey(tui))
			tui = tuiMap.get(tui);
		else
			tuiMap.put(tui, tui);
		Set<String> tuis = cuiTuiMap.get(cui);
		if (tuis == null) {
			tuis = new HashSet<String>();
			cuiTuiMap.put(cui, tuis);
		}
		tuis.add(tui);
	}

	// private String createKey(String c1, String c2) {
	// if (c1.compareTo(c2) < 0) {
	// return new StringBuilder(c1).append("-").append(c2).toString();
	// } else {
	// return new StringBuilder(c2).append("-").append(c1).toString();
	// }
	// }

	/**
	 * return lin measure. optionally filter lin measure so that only concepts
	 * that have an lcs that is relevant to the classification task have a
	 * non-zero lin measure.
	 * 
	 * relevant concepts are those whose evaluation wrt the label exceeds a
	 * threshold.
	 * 
	 * @param concept1
	 * @param concept2
	 * @param label
	 *            if not null, then filter lcses.
	 * @param lcsMinEvaluation
	 *            if gt; 0, then filter lcses. this is the threshold.
	 * @return 0 - no lcs, or no lcs that meets the threshold.
	 */
	@Override
	public double filteredLin(String concept1, String concept2,
			Map<String, Double> conceptFilter) {
		double ic1 = getIC(concept1);
		double ic2 = getIC(concept2);
		// lin not defined if one of the concepts doesn't exist in the corpus
		if (ic1 == 0 || ic2 == 0)
			return 0;
		double denom = getIC(concept1) + getIC(concept2);
		if (denom != 0) {
			ConcRel cr1 = cg.getConceptMap().get(concept1);
			ConcRel cr2 = cg.getConceptMap().get(concept2);
			if (cr1 != null && cr2 != null) {
				Set<String> lcses = new HashSet<String>();
				int dist = getLCSFromCache(cr1, cr2, lcses);
				if (dist > 0) {
					double ic = getBestIC(lcses, conceptFilter);
					return 2 * ic / denom;
				}
			}
		}
		return 0;
	}

	/**
	 * get the information content for the concept with the highest evaluation
	 * greater than a specified threshold.
	 * 
	 * If threshold 0, get the lowest IC of all the lcs's.
	 * 
	 * @param lcses
	 *            the least common subsumers of a pair of concepts
	 * @param label
	 *            label against which feature was evaluated
	 * @param lcsMinEvaluation
	 *            threshold that the feature has to exceed. 0 for no filtering.
	 * @return 0 if no lcs that makes the cut. else find the lcs(es) with the
	 *         maximal evaluation, and return getIC on these lcses.
	 * 
	 * @see #getIC(Iterable)
	 */
	private double getBestIC(Set<String> lcses,
			Map<String, Double> conceptFilter) {
		if (conceptFilter != null) {
			double currentBest = -1;
			Set<String> bestLcses = new HashSet<String>();
			for (String lcs : lcses) {
				if (conceptFilter.containsKey(lcs)) {
					double lcsEval = conceptFilter.get(lcs);
					if (currentBest == -1 || lcsEval > currentBest) {
						bestLcses.clear();
						bestLcses.add(lcs);
						currentBest = lcsEval;
					} else if (currentBest == lcsEval) {
						bestLcses.add(lcs);
					}
				}
			}
			if (bestLcses.size() > 0) {
				return this.getIC(bestLcses);
			}
		} else {
			// unfiltered - get the lowest ic
			return this.getIC(lcses);
		}
		return 0;
	}

	@Override
	public Object[] getBestLCS(Set<String> lcses, boolean intrinsicIC,
			Map<String, Double> conceptFilter) {
		if (conceptFilter != null) {
			double currentBest = -1;
			Set<String> bestLcses = new HashSet<String>();
			for (String lcs : lcses) {
				if (conceptFilter.containsKey(lcs)) {
					double lcsEval = conceptFilter.get(lcs);
					if (currentBest == -1 || lcsEval > currentBest) {
						bestLcses.clear();
						bestLcses.add(lcs);
						currentBest = lcsEval;
					} else if (currentBest == lcsEval) {
						bestLcses.add(lcs);
					}
				}
			}
			if (bestLcses.size() > 0) {
				return this.getBestLCS(bestLcses,
						intrinsicIC ? this.intrinsicICMap : this.corpusICMap);
			} else {
				// no lcses made the cut
				return null;
			}
		} else {
			// unfiltered - get the lowest ic
			return this.getBestLCS(lcses, intrinsicIC ? this.intrinsicICMap
					: this.corpusICMap);
		}
	}

	public Object[] getBestLCS(Set<String> lcses, Map<String, Double> icMap) {
		double ic = 0;
		String bestLCS = null;
		for (String lcs : lcses) {
			Double ictmp = icMap.get(lcs);
			if (ictmp != null && ic < ictmp) {
				ic = ictmp;
				bestLCS = lcs;
			}
		}
		return new Object[] { bestLCS, ic };
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public ConceptDao getConceptDao() {
		return conceptDao;
	}

	@Override
	public ConceptGraph getConceptGraph() {
		return cg;
	}

	public String getConceptGraphName() {
		return conceptGraphName;
	}

	public String getConceptSetName() {
		return conceptSetName;
	}

	public String getCorpusName() {
		return corpusName;
	}

	@Override
	public Map<String, BitSet> getCuiTuiMap() {
		return cuiTuiMap;
	}

	/**
	 * get the concept with the lowest Information Content of all the LCSs.
	 * Functionality copied from umls interface.
	 * 
	 * @todo make this configurable/add a parameter - avg/min/max/median?
	 * @param lcses
	 * @return
	 */
	public double getIC(Iterable<String> lcses) {
		double ic = 0;
		for (String lcs : lcses) {
			double ictmp = getIC(lcs);
			if (ic < ictmp)
				ic = ictmp;
		}
		return ic;
	}

	public double getIC(String concept1) {
		Double dRetVal = corpusICMap.get(concept1);
		if (dRetVal != null)
			return (double) dRetVal;
		else
			return 0;
	}

	@Override
	public Double getIC(String concept, boolean intrinsicICMap) {
		Map<String, Double> icMap = intrinsicICMap ? this.intrinsicICMap
				: this.corpusICMap;
		return icMap.get(concept);
	}

	public int getLCS(String concept1, String concept2, Set<String> lcses,
			Map<String, List<List<String>>> lcsPathMap) {
		int lcsDist = 0;
		ConcRel cr1 = getConceptGraph().getConceptMap().get(concept1);
		ConcRel cr2 = getConceptGraph().getConceptMap().get(concept2);
		if (cr1 != null && cr2 != null) {
			if (lcsPathMap == null) {
				// no need to get paths which we don't cache - look in the cache
				lcsDist = getLCSFromCache(cr1, cr2, lcses);
			} else {
				// need to get paths - compute the lcses and their paths
				lcsDist = lcs(concept1, concept2, lcsPathMap);
				lcses.addAll(lcsPathMap.keySet());
			}
		} else {
			if (log.isDebugEnabled()) {
				if (cr1 == null)
					log.debug("could not find concept:" + concept1);
				if (cr2 == null)
					log.debug("could not find concept:" + concept2);
			}
		}
		return lcsDist;
	}

	@SuppressWarnings("unchecked")
	public int getLCSFromCache(ConcRel cr1, ConcRel cr2, Set<String> lcses) {
		OrderedPair<String> cacheKey = new OrderedPair<String>(
				cr1.getConceptID(), cr2.getConceptID());
		Element e = this.lcsCache.get(cacheKey);
		if (e != null) {
			// hit the cache - unpack the lcs
			if (e.getObjectValue() != null) {
				Object[] val = (Object[]) e.getObjectValue();
				lcses.addAll((Set<String>) val[1]);
				return (Integer) val[0];
			} else {
				return -1;
			}
		} else {
			// missed the cache - save the lcs
			Object[] val = null;
			Set<ConcRel> lcsCRSet = new HashSet<ConcRel>(2);
			int dist = ConcRel.getLeastCommonConcept(cr1, cr2, lcsCRSet, null);
			if (dist >= 0) {
				val = new Object[2];
				val[0] = dist;
				for (ConcRel cr : lcsCRSet) {
					lcses.add(cr.getConceptID());
				}
				val[1] = lcses;
			}
			e = new Element(cacheKey, val);
			this.lcsCache.put(e);
			return dist;
		}
	}

	public String getLcsImputedType() {
		return lcsImputedType;
	}

	public Map<SimilarityMetricEnum, SimilarityMetric> getSimilarityMetricMap() {
		return similarityMetricMap;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	@Override
	public List<String> getTuiList() {
		return this.tuiList;
	}

	public void init() {
		TransactionTemplate t = new TransactionTemplate(this.transactionManager);
		t.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
		t.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				cg = conceptDao.getConceptGraph(conceptGraphName);
				initInfoContent();
				initCuiTuiMapFromCorpus();
				initSimilarityMetricMap();
				return null;
			}
		});
		this.lcsCache = getCacheManager().getCache("lcsCache");
	}

	/**
	 * load cui-tui for the specified corpus from the MRSTY table
	 */
	public void initCuiTuiMapFromCorpus() {
		// don't duplicate tui strings to save memory
		SortedMap<String, String> tuiMap = new TreeMap<String, String>();
		Map<String, Set<String>> tmpTuiCuiMap = new HashMap<String, Set<String>>();
		List<Object[]> listCuiTui = this.classifierEvaluationDao
				.getCorpusCuiTuis(this.getCorpusName(),
						this.getConceptGraphName(), this.getConceptSetName());
		for (Object[] cuiTui : listCuiTui) {
			String cui = (String) cuiTui[0];
			String tui = (String) cuiTui[1];
			addCuiTuiToMap(tmpTuiCuiMap, tuiMap, cui, tui);
		}
		// map of tui - bitset index
		SortedMap<String, Integer> mapTuiIndex = new TreeMap<String, Integer>();
		// list of tuis corresponding to bitset indices
		List<String> tmpTuiList = new ArrayList<String>(tuiMap.size());
		int index = 0;
		for (String tui : tuiMap.keySet()) {
			mapTuiIndex.put(tui, index++);
			tmpTuiList.add(tui);
		}
		this.tuiList = Collections.unmodifiableList(tmpTuiList);
		// convert list of cuis into bitsets
		Map<String, BitSet> tmpCuiTuiBitsetMap = new HashMap<String, BitSet>();
		for (Map.Entry<String, Set<String>> cuiTuiMapEntry : tmpTuiCuiMap
				.entrySet()) {
			tmpCuiTuiBitsetMap.put(cuiTuiMapEntry.getKey(),
					tuiListToBitset(cuiTuiMapEntry.getValue(), mapTuiIndex));
		}
		this.cuiTuiMap = Collections.unmodifiableMap(tmpCuiTuiBitsetMap);
	}

	/**
	 * initialize information content caches TODO replace strings with concept
	 * ids from conceptGraph to save memory
	 */
	private void initInfoContent() {
		this.corpusICMap = classifierEvaluationDao.getInfoContent(corpusName,
				conceptGraphName, this.conceptSetName);
		this.intrinsicICMap = classifierEvaluationDao
				.getIntrinsicInfoContent(conceptGraphName);
	}

	/**
	 * initialize the metrics
	 */
	private void initSimilarityMetricMap() {
		this.similarityMetricMap = new HashMap<SimilarityMetricEnum, SimilarityMetric>(
				SimilarityMetricEnum.values().length);
		this.similarityMetricMap.put(SimilarityMetricEnum.LCH, new LCHMetric(
				this));
		this.similarityMetricMap.put(SimilarityMetricEnum.LIN, new LinMetric(
				this, false));
		this.similarityMetricMap.put(SimilarityMetricEnum.INTRINSIC_LIN,
				new LinMetric(this, true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.ConceptSimilarity#lch(java.lang.String,
	 * java.lang.String)
	 */
	public double lch(String concept1, String concept2) {
		double dm = 2 * cg.getDepthMax() + 1.0;
		ConcRel cr1 = cg.getConceptMap().get(concept1);
		ConcRel cr2 = cg.getConceptMap().get(concept2);
		if (cr1 != null && cr2 != null) {
			Set<String> lcses = new HashSet<String>();
			int lcsDist = getLCSFromCache(cr1, cr2, lcses);
			// leacock is defined as -log([path length]/(2*[depth])
			double lch = -Math.log(((double) lcsDist + 1.0) / dm);
			// scale to depth
			return lch / Math.log(dm);
		} else {
			if (log.isDebugEnabled()) {
				if (cr1 == null)
					log.debug("could not find concept:" + concept1);
				if (cr2 == null)
					log.debug("could not find concept:" + concept2);
			}
			return 0;
		}
	}

	public int lcs(String concept1, String concept2,
			Map<String, List<List<String>>> lcsPath) {
		ConcRel cr1 = cg.getConceptMap().get(concept1);
		ConcRel cr2 = cg.getConceptMap().get(concept2);
		int dist = -1;
		if (cr1 != null && cr2 != null) {
			Set<ConcRel> crlcses = new HashSet<ConcRel>();
			Map<ConcRel, List<List<ConcRel>>> crpaths = new HashMap<ConcRel, List<List<ConcRel>>>();
			dist = ConcRel.getLeastCommonConcept(cr1, cr2, crlcses, crpaths);
			for (Map.Entry<ConcRel, List<List<ConcRel>>> crLcsPath : crpaths
					.entrySet()) {
				List<List<String>> lcsPaths = new ArrayList<List<String>>(2);
				for (List<ConcRel> crpath : crLcsPath.getValue()) {
					List<String> lcsPathEntry = new ArrayList<String>(
							crpath.size());
					for (ConcRel cr : crpath)
						lcsPathEntry.add(cr.getConceptID());
					lcsPaths.add(lcsPathEntry);
				}
				lcsPath.put(crLcsPath.getKey().getConceptID(), lcsPaths);
			}
		}
		return dist;
	}

	public double lin(String concept1, String concept2) {
		return filteredLin(concept1, concept2, null);
	}

	/**
	 * For the given label and cutoff, get the corresponding concepts whose
	 * propagated ig meets the threshold. Used by lin kernel to find concepts
	 * that actually have a non-trivial similarity
	 * 
	 * @param label
	 *            label
	 * @param rankCutoff
	 *            cutoff
	 * @param conceptFilter
	 *            set to fill with concepts
	 * @return double minimum evaluation
	 */
	@Override
	public double loadConceptFilter(String label, int rankCutoff,
			Map<String, Double> conceptFilter) {
		List<FeatureRank> imputedConcepts = this.classifierEvaluationDao
				.getImputedFeaturesByPropagatedCutoff(corpusName,
						conceptSetName, label, lcsImputedType
								+ ImputedFeatureEvaluator.SUFFIX_IMPUTED,
						conceptGraphName, lcsImputedType
								+ ImputedFeatureEvaluator.SUFFIX_PROP,
						rankCutoff);
		double minEval = 1d;
		for (FeatureRank r : imputedConcepts) {
			conceptFilter.put(r.getFeatureName(), r.getEvaluation());
			if (minEval >= r.getEvaluation())
				minEval = r.getEvaluation();
		}
		return minEval;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public void setConceptDao(ConceptDao conceptDao) {
		this.conceptDao = conceptDao;
	}

	public void setConceptGraphName(String conceptGraphName) {
		this.conceptGraphName = conceptGraphName;
	}

	public void setConceptSetName(String conceptSetName) {
		this.conceptSetName = conceptSetName;
	}

	// double minEval = 1d;
	// List<FeatureRank> listPropagatedConcepts = classifierEvaluationDao
	// .getTopFeatures(corpusName, conceptSetName, label,
	// ImputedFeatureEvaluator.MeasureType.INFOGAIN.toString()
	// + ImputedFeatureEvaluator.SUFFIX_PROP, 0, 0,
	// conceptGraphName, rankCutoff);
	// for (FeatureRank r : listPropagatedConcepts) {
	// ConcRel cr = cg.getConceptMap().get(r.getFeatureName());
	// if (cr != null) {
	// addSubtree(conceptFilterSet, cr);
	// }
	// if (r.getEvaluation() < minEval)
	// minEval = r.getEvaluation();
	// }
	// return minEval;
	// }
	//
	// /**
	// * add all children of parent to conceptSet. Limit only to children that
	// * actually appear in the corpus
	// *
	// * @param conceptSet
	// * set of concepts to add ids to
	// * @param parent
	// * parent which will be added to the conceptSet
	// * @param corpusICSet
	// * set of concepts and hypernyms contained in corpus
	// */
	// private void addSubtree(Map<String, Double> conceptSet, ConcRel parent) {
	// if (!conceptSet.containsKey(parent.getConceptID())
	// && conceptFreq.containsKey(parent.getConceptID())) {
	// conceptSet.put(parent.getConceptID(), 0d);
	// for (ConcRel child : parent.getChildren()) {
	// addSubtree(conceptSet, child);
	// }
	// }
	// }

	public void setCorpusName(String corpusName) {
		this.corpusName = corpusName;
	}

	public void setLcsImputedType(String lcsImputedType) {
		this.lcsImputedType = lcsImputedType;
	}

	public void setSimilarityMetricMap(
			Map<SimilarityMetricEnum, SimilarityMetric> similarityMetricMap) {
		this.similarityMetricMap = similarityMetricMap;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public List<Map<SimilarityMetricEnum, Double>> similarity(
			List<ConceptPair> conceptPairs, Set<SimilarityMetricEnum> metrics,
			Map<String, Double> conceptFilter, List<SimilarityInfo> simInfos) {
		List<Map<SimilarityMetricEnum, Double>> conceptSimMap = new ArrayList<Map<SimilarityMetricEnum, Double>>(
				conceptPairs.size());
		for (ConceptPair conceptPair : conceptPairs) {
			SimilarityInfo simInfo = null;
			if (simInfos != null) {
				simInfo = new SimilarityInfo();
				simInfo.setLcsPathMap(new HashMap<String, List<List<String>>>());
			}
			conceptSimMap.add(similarity(metrics, conceptPair.getConcept1(),
					conceptPair.getConcept2(), conceptFilter, simInfo));
			if (simInfos != null) {
				simInfos.add(simInfo);
			}
		}
		return conceptSimMap;
	}

	/**
	 * 
	 */
	@Override
	public Map<SimilarityMetricEnum, Double> similarity(
			Set<SimilarityMetricEnum> metrics, String concept1,
			String concept2, Map<String, Double> conceptFilter,
			SimilarityInfo simInfo) {
		// allocate simInfo if this isn't provided
		if (simInfo == null)
			simInfo = new SimilarityInfo();
		// allocate result map
		Map<SimilarityMetricEnum, Double> metricSimMap = new HashMap<SimilarityMetricEnum, Double>(
				metrics.size());
		// iterate over metrics, compute, stuff in map
		for (SimilarityMetricEnum metric : metrics) {
			double sim = this.similarityMetricMap.get(metric).similarity(
					concept1, concept2, conceptFilter, simInfo);
			metricSimMap.put(metric, sim);
		}
		return metricSimMap;
	}

	/**
	 * convert the list of tuis into a bitset
	 * 
	 * @param tuis
	 * @param mapTuiIndex
	 * @return
	 */
	private BitSet tuiListToBitset(Set<String> tuis,
			SortedMap<String, Integer> mapTuiIndex) {
		BitSet bs = new BitSet(mapTuiIndex.size());
		for (String tui : tuis) {
			bs.set(mapTuiIndex.get(tui));
		}
		return bs;
	}
}
