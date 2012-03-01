package ytex.kernel.metric;

import java.util.Map;

public class LCHMetric extends BaseSimilarityMetric {
	/**
	 * log(max depth * 2)
	 */
	double logdm = 0d;

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		if (logdm != 0d) {
			initLCSes(concept1, concept2, simInfo);
			if (simInfo.getLcsDist() > 0) {
				// double lch = logdm - Math.log((double) simInfo.getLcsDist());
				// // scale to depth
				// return lch / logdm;
				return 1 - (Math.log((double) simInfo.getLcsDist()) / logdm);
			}
		}
		return 0d;
	}

	public LCHMetric(ConceptSimilarityService simSvc, Integer maxDepth) {
		super(simSvc);
		if (maxDepth != null) {
			this.logdm = Math.log(2 * maxDepth);
		}
	}

}
