package ytex.kernel.metric;

import java.util.Map;

public class WuPalmerMetric extends BaseSimilarityMetric {
	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		initLCSes(concept1, concept2, simInfo);
		if (simInfo.getLcses().size() > 0) {
			int lcsDepth = 0;
			for (String lcs : simInfo.getLcses()) {
				int d = simSvc.getDepth(lcs);
				if (d > lcsDepth)
					lcsDepth = d;
			}
			double lcsDepth2 = (double) (lcsDepth * 2);
			return lcsDepth2 / (lcsDepth2 + (double) (simInfo.getLcsDist()-1));
		}
		return 0d;
	}

	public WuPalmerMetric(ConceptSimilarityService simSvc) {
		super(simSvc);
	}

}
