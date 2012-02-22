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
		initLCSes(concept1, concept2, simInfo);
		if (simInfo.getLcsDist() > 0) {
			double lch = logdm - Math.log((double) simInfo.getLcsDist());
			// scale to depth
			return lch / logdm;
		} else {
			return 0;
		}
	}

	public LCHMetric(ConceptSimilarityService simSvc) {
		super(simSvc);
		double depth = this.simSvc.getConceptGraph().getDepthMax();
		this.logdm = Math.log(2 * this.simSvc.getConceptGraph().getDepthMax());
	}

}
