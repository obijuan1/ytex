package ytex.kernel.metric;

import java.util.Map;

import ytex.kernel.ConceptSimilarityService;

/**
 * compute intrinsic LCH as in eqn 28 from
 * http://dx.doi.org/10.1016/j.jbi.2011.03.013
 * 
 * @author vijay
 * 
 */
public class IntrinsicLCHMetric extends BaseSimilarityMetric {
	Double maxIC;

	public IntrinsicLCHMetric(ConceptSimilarityService simSvc, Double maxIC) {
		super(simSvc);
		this.maxIC = maxIC;
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		double sim = 0d;
		if (maxIC != null) {
			double ic1 = simSvc.getIC(concept1, true);
			double ic2 = simSvc.getIC(concept2, true);
			double lcsIC = initLcsIC(concept1, concept2, conceptFilter,
					simInfo, true);
			sim = Math.log(2 * maxIC.doubleValue())
					- Math.log(ic1 + ic2 - 2 * (lcsIC) + 1);
		}
		return sim;
	}

}
