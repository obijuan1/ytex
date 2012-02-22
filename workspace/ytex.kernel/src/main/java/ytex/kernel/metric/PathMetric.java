package ytex.kernel.metric;

import java.util.Map;


public class PathMetric extends BaseSimilarityMetric {

	public PathMetric(ConceptSimilarityService simSvc) {
		super(simSvc);
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		this.initLCSes(concept1, concept2, simInfo);
		if (simInfo.getLcsDist() > 0) {
			return 1 / ((double) simInfo.getLcsDist());
		} else {
			return 0;
		}
	}

}
