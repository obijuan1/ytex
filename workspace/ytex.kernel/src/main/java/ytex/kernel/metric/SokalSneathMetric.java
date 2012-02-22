package ytex.kernel.metric;

import java.util.Map;


/**
 * Sokal and Sneath metric as in eqn 18 from
 * http://dx.doi.org/10.1016/j.jbi.2011.03.013
 * 
 * @author vijay
 * 
 */
public class SokalSneathMetric extends BaseSimilarityMetric {

	public SokalSneathMetric(ConceptSimilarityService simSvc) {
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
		return lcsIC / (2 * (ic1 + ic2) - 3 * lcsIC);
	}

}
