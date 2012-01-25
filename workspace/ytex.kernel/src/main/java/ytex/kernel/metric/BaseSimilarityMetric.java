package ytex.kernel.metric;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ytex.kernel.ConceptSimilarityService;

public abstract class BaseSimilarityMetric implements SimilarityMetric {

	protected ConceptSimilarityService simSvc;

	public ConceptSimilarityService getConceptSimilarityService() {
		return simSvc;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.simSvc = conceptSimilarityService;
	}

	/**
	 * compute the lcses and min path distance for the concept pair, if this
	 * hasn't been done already
	 * 
	 * @param concept1
	 * @param concept2
	 * @param simInfo
	 */
	protected void initLCSes(String concept1, String concept2,
			SimilarityInfo simInfo) {
		if (simInfo.getLcsDist() == null) {
			Set<String> lcses = new HashSet<String>(1);
			simInfo.setLcses(lcses);
			simInfo.setLcsDist(simSvc.getLCS(concept1, concept2, lcses,
					simInfo.getLcsPathMap()));
		}
	}

	/**
	 * get the best lcs and its information content if this hasn't been done
	 * already.
	 * 
	 * @param conceptFilter
	 * @param simInfo
	 * @param intrinsicIC
	 *            set to false for corpus based ic
	 * @return
	 */
	protected double initLcsIC(Map<String, Double> conceptFilter,
			SimilarityInfo simInfo, boolean intrinsicIC) {
		Double lcsIC = intrinsicIC ? simInfo.getIntrinsicLcsIC() : simInfo
				.getCorpusLcsIC();
		if (lcsIC == null) {
			String lcs = null;
			lcsIC = 0d;
			Object[] bestLCSArr = simSvc.getBestLCS(simInfo.getLcses(),
					intrinsicIC, conceptFilter);
			if (bestLCSArr != null) {
				lcs = (String) bestLCSArr[0];
				lcsIC = (Double) bestLCSArr[1];
				if (intrinsicIC) {
					simInfo.setIntrinsicLcs(lcs);
					simInfo.setIntrinsicLcsIC(lcsIC);
				} else {
					simInfo.setCorpusLcs(lcs);
					simInfo.setCorpusLcsIC(lcsIC);
				}
			}
		}
		return lcsIC;
	}

	/**
	 * call initLCSes and initLcsIC
	 * 
	 * @param concept1
	 * @param concept2
	 * @param conceptFilter
	 * @param simInfo
	 * @param intrinsicIC
	 * @return
	 */
	protected double initLcsIC(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo,
			boolean intrinsicIC) {
		this.initLCSes(concept1, concept2, simInfo);
		return this.initLcsIC(conceptFilter, simInfo, intrinsicIC);
	}

	public BaseSimilarityMetric(ConceptSimilarityService simSvc) {
		this.simSvc = simSvc;
	}

}
