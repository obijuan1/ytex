package ytex.kernel.metric;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.ConceptSimilarityService;

/**
 * compute corpus-ic or intrinsic-ic based lin measure.
 * 
 * @author vijay
 * 
 */
public class LinMetric extends BaseSimilarityMetric {
	private static final Log log = LogFactory.getLog(LinMetric.class);
	private boolean intrinsicIC = true;

	public boolean isIntrinsicIC() {
		return intrinsicIC;
	}

	public void setIntrinsicIC(boolean intrinsicIC) {
		this.intrinsicIC = intrinsicIC;
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		initLCSes(concept1, concept2, simInfo);
		if (simInfo.getLcsPaths() == null || simInfo.getLcsPaths().isEmpty())
			return 0d;
		double lcsIC = initLcsIC(conceptFilter, simInfo, this.intrinsicIC);
		if (lcsIC == 0d) {
			return 0d;
		}
		double denom = simSvc.getIC(concept1, this.intrinsicIC)
				+ simSvc.getIC(concept2, this.intrinsicIC);
		if (denom == 0)
			return 0d;
		return 2 * lcsIC / denom;
	}

	public LinMetric(ConceptSimilarityService simSvc, boolean intrinsicIC) {
		super(simSvc);
		this.intrinsicIC = intrinsicIC;
	}

}
