package ytex.kernel.metric;

import java.util.Map;

import ytex.kernel.ConceptSimilarityService;

/**
 * Jaccard metric as in eqn 13 from http://dx.doi.org/10.1016/j.jbi.2011.03.013
 * 
 * @author vijay
 * 
 */
public class JaccardMetric extends BaseSimilarityMetric {

	public JaccardMetric(ConceptSimilarityService simSvc) {
		super(simSvc);
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		double lcsIC = this.initLcsIC(concept1, concept2, conceptFilter,
				simInfo, true);
		if (lcsIC == 0d)
			return 0d;
		double ic1 = simSvc.getIC(concept1, true);
		double ic2 = simSvc.getIC(concept2, true);
		return lcsIC / (ic1 + ic2 - lcsIC);
	}

}
