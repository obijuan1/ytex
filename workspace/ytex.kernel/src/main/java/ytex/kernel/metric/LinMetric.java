package ytex.kernel.metric;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * compute corpus-ic or intrinsic-ic based lin measure.
 * 
 * @author vijay
 * 
 */
public class LinMetric extends BaseSimilarityMetric {
	private static final Log log = LogFactory.getLog(LinMetric.class);
	private boolean intrinsicIC = true;
	private boolean validCG = false;
	private String rootConcept = simSvc.getConceptGraph().getRoot();

	public boolean isIntrinsicIC() {
		return intrinsicIC;
	}

	public void setIntrinsicIC(boolean intrinsicIC) {
		this.intrinsicIC = intrinsicIC;
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		// don't bother if the concept graph is null
		if (!validCG)
			return 0d;
		// get lcs
		double lcsIC = initLcsIC(concept1, concept2, conceptFilter, simInfo,
				this.intrinsicIC);
		if (lcsIC == 0d) {
			return 0d;
		}
		// get ic of concepts
		double ic1 = simSvc.getIC(concept1, this.intrinsicIC);
		double ic2 = simSvc.getIC(concept2, this.intrinsicIC);
		// if the corpus IC is 0 and the concept is not the root, then we don't
		// have any IC on the concept and can't measure similarity - return 0
		if (!intrinsicIC && ic1 == 0 && !rootConcept.equals(concept1))
			return 0d;
		if (!intrinsicIC && ic2 == 0 && !rootConcept.equals(concept2))
			return 0d;
		double denom = ic1 + ic2;
		if (denom == 0)
			return 0d;
		return 2 * lcsIC / denom;
	}

	public LinMetric(ConceptSimilarityService simSvc, boolean intrinsicIC) {
		super(simSvc);
		this.intrinsicIC = intrinsicIC;
		this.validCG = simSvc.getConceptGraph() != null;
		if (!this.intrinsicIC && validCG) {
			rootConcept = simSvc.getConceptGraph().getRoot();
		}
	}

}
