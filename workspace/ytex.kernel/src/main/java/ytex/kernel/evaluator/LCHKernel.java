package ytex.kernel.evaluator;

import ytex.kernel.ConceptSimilarityService;

public class LCHKernel extends CacheKernel {
	private ConceptSimilarityService conceptSimilarityService;

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	@Override
	public double innerEvaluate(Object o1, Object o2) {
		double d = 0;
		String c1 = (String) o1;
		String c2 = (String) o2;
		if (c1 != null && c2 != null) {
			if (c1.equals(c2)) {
				d = 1;
			} else {
				d = conceptSimilarityService.lch(c1, c2);
			}
		}
		return d;
	}

}
