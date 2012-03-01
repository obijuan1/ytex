package ytex.kernel.metric;

import java.util.Map;


/**
 * compute Intrinsic rada distance as in eqn 23 from
 * http://dx.doi.org/10.1016/j.jbi.2011.03.013. Scale the distance to the unit
 * interval using max IC. Convert to similarity metric by taking
 * 1-scaled_distance.
 * 
 * @author vijay
 * 
 */
public class IntrinsicRadaMetric extends BaseSimilarityMetric {
	Double maxIC;

	public IntrinsicRadaMetric(ConceptSimilarityService simSvc, Double maxIC) {
		super(simSvc);
		this.maxIC = maxIC;
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		if (maxIC == null)
			return 0d;
		double lcsIC = this.initLcsIC(concept1, concept2, conceptFilter, simInfo, true);
		if (lcsIC == 0d)
			return 0d;
		double ic1 = simSvc.getIC(concept1, true);
		double ic2 = simSvc.getIC(concept2, true);
		// scale to unit interval
		return 1d - (ic1 + ic2 - (2 * lcsIC)) / (2 * maxIC);
	}

}
