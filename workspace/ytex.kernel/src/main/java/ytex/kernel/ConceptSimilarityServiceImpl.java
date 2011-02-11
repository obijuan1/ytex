package ytex.kernel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import ytex.kernel.model.Corpus;
import ytex.kernel.model.CorpusTerm;
import ytex.kernel.model.InfoContent;
import ytex.kernel.model.ObjPair;

public class ConceptSimilarityServiceImpl implements ConceptSimilarityService {
	private static final Log log = LogFactory
			.getLog(ConceptSimilarityServiceImpl.class);
	private ConceptDao conceptDao;
	private CorpusDao corpusDao;
	ConceptGraph cg = null;
	private PlatformTransactionManager transactionManager;

	/**
	 * information concept cache
	 */
	private Map<String, Map<String, Double>> corporaIC = null;
	private List<String> corpusNames;

	public List<String> getCorpusNames() {
		return corpusNames;
	}

	public void setCorpusNames(List<String> corpusNames) {
		this.corpusNames = corpusNames;
	}

	public ConceptDao getConceptDao() {
		return conceptDao;
	}

	public void setConceptDao(ConceptDao conceptDao) {
		this.conceptDao = conceptDao;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public double getIC(String corpusName, String concept1) {
		Map<String, Double> conceptFreq = corporaIC.get(corpusName);
		if (conceptFreq != null) {
			Double dRetVal = conceptFreq.get(concept1);
			if (dRetVal != null)
				return (double) dRetVal;
		}
		return 0;
	}

	public Object[] lcs(String concept1, String concept2) {
		ConcRel cr1 = cg.getConceptMap().get(concept1);
		ConcRel cr2 = cg.getConceptMap().get(concept2);
		if (cr1 != null && cr2 != null) {
			ObjPair<ConcRel, Integer> op = ConcRel.getLeastCommonConcept(cr1,
					cr2);
			if (op != null) {
				return new Object[] { op.v1.nodeCUI, op.v2 };
			}
		}
		return null;
	}

	public double lin(String corpusName, String concept1, String concept2) {
		// if(log.isDebugEnabled())
		// log.debug("lin("+corpusName+", " + concept1 +"," +concept2+")");
		double denom = getIC(corpusName, concept1)
				+ getIC(corpusName, concept2);
		if (denom != 0) {
			ConcRel cr1 = cg.getConceptMap().get(concept1);
			ConcRel cr2 = cg.getConceptMap().get(concept2);
			if (cr1 != null && cr2 != null) {
				ObjPair<ConcRel, Integer> op = ConcRel.getLeastCommonConcept(
						cr1, cr2);
				if (op != null && op.v1 != null) {
					return 2 * getIC(corpusName, op.v1.nodeCUI) / denom;
				}
			}
		}
		return 0;
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
			ObjPair<ConcRel, Integer> op = ConcRel.getLeastCommonConcept(cr1,
					cr2);
			// leacock is defined as -log([path length]/(2*[depth])
			double lch = -Math.log(((double) op.v2.intValue() + 1.0) / dm);
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

	/**
	 * recursively sum frequency of parent and all its childrens' frequencies
	 * 
	 * @param parent
	 *            parent node
	 * @param conceptFreq
	 *            results stored here
	 * @param conceptIdToTermMap
	 *            raw frequencies here
	 * @return double sum of concept frequency in the subtree with parent as
	 *         root
	 */
	double getFrequency(ConcRel parent, Map<String, Double> conceptFreq,
			Map<String, CorpusTerm> conceptIdToTermMap) {
		double dFreq = 0;
		// get raw freq
		CorpusTerm t = conceptIdToTermMap.get(parent.nodeCUI);
		if (t != null) {
			dFreq = t.getFrequency();
		}
		// recurse
		for (ConcRel child : parent.children) {
			dFreq += getFrequency(child, conceptFreq, conceptIdToTermMap);
		}
		conceptFreq.put(parent.nodeCUI, dFreq);
		return dFreq;
	}

	/**
	 * initialize information content caches
	 */
	public void initICCorpora() {
		this.corporaIC = new HashMap<String, Map<String, Double>>(
				getCorpusNames().size());
		for (InfoContent ic : corpusDao.getInfoContent(getCorpusNames())) {
			Map<String, Double> corpusFreq = corporaIC.get(ic.getCorpus()
					.getCorpusName());
			if (corpusFreq == null) {
				corpusFreq = new HashMap<String, Double>();
				corporaIC.put(ic.getCorpus().getCorpusName(), corpusFreq);
			}
			corpusFreq.put(ic.getConceptId(), ic.getInformationContent());
		}
	}

	/**
	 * calculate information content for all concepts
	 */
	public void updateInformationContent(String corpusName) {
		List<CorpusTerm> terms = corpusDao.getTerms(corpusName);
		Corpus corpus = terms.get(0).getCorpus();
		double totalFreq = 0;
		//map of cui to term
		Map<String, CorpusTerm> conceptIdToTermMap = new HashMap<String, CorpusTerm>(
				terms.size());
		for (CorpusTerm t : terms) {
			conceptIdToTermMap.put(t.getConceptId(), t);
		}
		//map of cui to cumulative frequency
		Map<String, Double> conceptFreq = new HashMap<String, Double>(cg
				.getConceptMap().size());
		//recurse through the tree
		for (String conceptId : cg.getRoots()) {
			totalFreq += getFrequency(cg.getConceptMap().get(conceptId),
					conceptFreq, conceptIdToTermMap);
		}
		//update information content
		for (Map.Entry<String, Double> cfreq : conceptFreq.entrySet()) {
			InfoContent ic = new InfoContent();
			if (cfreq.getValue() > 0) {
				ic.setConceptId(cfreq.getKey());
				ic.setCorpus(corpus);
				ic.setFrequency(cfreq.getValue());
				ic.setInformationContent(-Math.log(cfreq.getValue() / totalFreq));
				corpusDao.addInfoContent(ic);
			}
		}

	}

	public void init() {
		TransactionTemplate t = new TransactionTemplate(this.transactionManager);
		t.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
		t.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				cg = conceptDao.initializeConceptGraph(new String[] {});
				initICCorpora();
				return null;
			}
		});
	}

	public void setCorpusDao(CorpusDao corpusDao) {
		this.corpusDao = corpusDao;
	}

	public CorpusDao getCorpusDao() {
		return corpusDao;
	}

	public static void main(String args[]) {
		if (args.length >= 2) {
			ConceptSimilarityService s = (ConceptSimilarityService) SimSvcContextHolder
					.getApplicationContext()
					.getBean("conceptSimilarityService");
			if (args[0].equals("-updateIC")) {
				s.updateInformationContent(args[1]);
			}
		}
	}
}
