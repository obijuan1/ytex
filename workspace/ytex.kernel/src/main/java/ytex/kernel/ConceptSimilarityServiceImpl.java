package ytex.kernel;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureRank;

public class ConceptSimilarityServiceImpl implements ConceptSimilarityService {
	private static final Log log = LogFactory
			.getLog(ConceptSimilarityServiceImpl.class);
	private CacheManager cacheManager;
	private ConceptGraph cg = null;
	private ClassifierEvaluationDao classifierEvaluationDao;
	private ConceptDao conceptDao;
	/**
	 * information concept cache
	 */
	private Map<String, Double> conceptFreq = null;
	private String conceptGraphName;

	private String conceptSetName;

	private String corpusName;
	private Map<String, BitSet> cuiTuiMap;
	/**
	 * cache to hold lcs's
	 */
	private Cache lcsCache;
	private String lcsImputedType = ImputedFeatureEvaluator.MeasureType.INFOGAIN
			.getName();
	private PlatformTransactionManager transactionManager;

	private List<String> tuiList;

	/*
	 * valid lcs cache
	 */
	private Map<String, Map<String, FeatureRank>> validLCSCache;

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
				Set<ConcRel> lcses = new HashSet<ConcRel>();
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
	private double getBestIC(Set<ConcRel> lcses,
			Map<String, Double> conceptFilter) {
		if (conceptFilter != null) {
			double currentBest = -1;
			Set<ConcRel> bestLcses = new HashSet<ConcRel>();
			for (ConcRel lcs : lcses) {
				if (conceptFilter.containsKey(lcs.getConceptID())) {
					double lcsEval = conceptFilter.get(lcs.getConceptID());
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

	// private String createKey(String c1, String c2) {
	// if (c1.compareTo(c2) < 0) {
	// return new StringBuilder(c1).append("-").append(c2).toString();
	// } else {
	// return new StringBuilder(c2).append("-").append(c1).toString();
	// }
	// }

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
	public double getIC(Iterable<ConcRel> lcses) {
		double ic = 0;
		for (ConcRel lcs : lcses) {
			double ictmp = getIC(lcs.getConceptID());
			if (ic < ictmp)
				ic = ictmp;
		}
		return ic;
	}

	public double getIC(String concept1) {
		Double dRetVal = conceptFreq.get(concept1);
		if (dRetVal != null)
			return (double) dRetVal;
		else
			return 0;
	}

	@SuppressWarnings("unchecked")
	private int getLCSFromCache(ConcRel cr1, ConcRel cr2, Set<ConcRel> lcses) {
		OrderedPair<String> cacheKey = new OrderedPair<String>(
				cr1.getConceptID(), cr2.getConceptID());
		Element e = this.lcsCache.get(cacheKey);
		if (e != null) {
			// hit the cache - unpack the lcs
			if (e.getObjectValue() != null) {
				Object[] val = (Object[]) e.getObjectValue();
				for (String lcs : (Set<String>) val[1]) {
					lcses.add(this.cg.getConceptMap().get(lcs));
				}
				return (Integer) val[0];
			} else {
				return -1;
			}
		} else {
			// missed the cache - save the lcs
			Object[] val = null;
			int dist = ConcRel.getLeastCommonConcept(cr1, cr2, lcses, null);
			if (dist >= 0) {
				val = new Object[2];
				val[0] = dist;
				Set<String> lcsStrSet = new HashSet<String>(lcses.size());
				for (ConcRel cr : lcses) {
					lcsStrSet.add(cr.getConceptID());
				}
				val[1] = lcsStrSet;
			}
			e = new Element(cacheKey, val);
			this.lcsCache.put(e);
			return dist;
		}
	}

	public String getLcsImputedType() {
		return lcsImputedType;
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
				initValidLCSCache();
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
	 * initialize information content caches
	 */
	public void initInfoContent() {
		conceptFreq = classifierEvaluationDao.getInfoContent(corpusName,
				conceptGraphName, this.conceptSetName);
	}

	public void initValidLCSCache() {
		// List<FeatureEvaluation> feList = this.classifierEvaluationDao
		// .getFeatureEvaluations(this.corpusName, this.conceptSetName,
		// this.lcsImputedType
		// + ImputedFeatureEvaluator.SUFFIX_IMPUTED, 0d,
		// this.conceptGraphName);
		this.validLCSCache = new HashMap<String, Map<String, FeatureRank>>();
		// for (FeatureEvaluation r : feList) {
		// Map<String, FeatureRank> featureMap = new HashMap<String,
		// FeatureRank>();
		// this.validLCSCache.put(r.getLabel(), featureMap);
		// for (FeatureRank rank : r.getFeatures()) {
		// featureMap.put(rank.getFeatureName(), rank);
		// }
		// }
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
			Set<ConcRel> lcses = new HashSet<ConcRel>();
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

	public void setCorpusName(String corpusName) {
		this.corpusName = corpusName;
	}

	public void setLcsImputedType(String lcsImputedType) {
		this.lcsImputedType = lcsImputedType;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
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
}
