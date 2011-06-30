package ytex.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ConceptDao;
import ytex.kernel.dao.CorpusDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;

public class ConceptSimilarityServiceImpl implements ConceptSimilarityService {
	private static final Log log = LogFactory
			.getLog(ConceptSimilarityServiceImpl.class);
	private ConceptGraph cg = null;
	private ConceptDao conceptDao;
	/**
	 * information concept cache
	 */
	private Map<String, Double> conceptFreq = null;
	private String conceptGraphName;
	private String conceptSetName;
	private CorpusDao corpusDao;

	private String corpusName;

	private Map<String, Set<String>> cuiTuiMap;

	private PlatformTransactionManager transactionManager;

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

	public CorpusDao getCorpusDao() {
		return corpusDao;
	}

	public String getCorpusName() {
		return corpusName;
	}

	@Override
	public Map<String, Set<String>> getCuiTuiMap() {
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

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
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
				return null;
			}
		});
	}

	/**
	 * load cui-tui for the specified corpus from the MRSTY table
	 */
	public void initCuiTuiMapFromCorpus() {
		// don't duplicate tui strings to save memory
		Map<String, String> tuiMap = new HashMap<String, String>();
		Map<String, Set<String>> tmpTuiCuiMap = new HashMap<String, Set<String>>();
		List<Object[]> listCuiTui = this.getCorpusDao().getCorpusCuiTuis(
				this.getCorpusName(), this.getConceptGraphName(),
				this.getConceptSetName());
		for (Object[] cuiTui : listCuiTui) {
			String cui = (String) cuiTui[0];
			String tui = (String) cuiTui[1];
			addCuiTuiToMap(tmpTuiCuiMap, tuiMap, cui, tui);
		}
		cuiTuiMap = Collections.unmodifiableMap(tmpTuiCuiMap);
	}

	/**
	 * initialize information content caches
	 */
	public void initInfoContent() {
		conceptFreq = corpusDao.getInfoContent(corpusName, conceptGraphName,
				this.conceptSetName);
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
			// ObjPair<ConcRel, Integer> op = ConcRel.getLeastCommonConcept(cr1,
			// cr2);
			int lcsDist = ConcRel.getLeastCommonConcept(cr1, cr2, lcses, null);
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
		// if(log.isDebugEnabled())
		// log.debug("lin("+corpusName+", " + concept1 +"," +concept2+")");
		double denom = getIC(concept1) + getIC(concept2);
		if (denom != 0) {
			ConcRel cr1 = cg.getConceptMap().get(concept1);
			ConcRel cr2 = cg.getConceptMap().get(concept2);
			if (cr1 != null && cr2 != null) {
				Set<ConcRel> lcses = new HashSet<ConcRel>();
				int dist = ConcRel.getLeastCommonConcept(cr1, cr2, lcses, null);
				if (dist > 0) {
					return 2 * getIC(lcses) / denom;
				}
			}
		}
		return 0;
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

	public void setCorpusDao(CorpusDao corpusDao) {
		this.corpusDao = corpusDao;
	}

	public void setCorpusName(String corpusName) {
		this.corpusName = corpusName;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
}
