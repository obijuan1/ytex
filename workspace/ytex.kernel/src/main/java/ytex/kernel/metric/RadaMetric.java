package ytex.kernel.metric;

import java.util.Map;

/**
 * 1 - path / (2*maxDepth)
 * 
 * @author vijay
 * 
 */
public class RadaMetric extends BaseSimilarityMetric {

	double depthMax = 0d;

	public RadaMetric(ConceptSimilarityService simSvc, Integer depthMax) {
		super(simSvc);
		if (depthMax != null)
			this.depthMax = depthMax.doubleValue();
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		if (depthMax == 0d)
			return 0d;
		this.initLCSes(concept1, concept2, simInfo);
		if (simInfo.getLcsDist() > 0) {
			return 1 - (((double) simInfo.getLcsDist() - 1) / (double) (2 * depthMax));
		} else {
			return 0;
		}
	}

}
