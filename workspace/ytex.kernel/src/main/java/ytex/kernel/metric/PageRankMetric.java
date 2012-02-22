package ytex.kernel.metric;

import java.util.Map;

import ytex.kernel.pagerank.PageRankService;

public class PageRankMetric extends BaseSimilarityMetric {
	PageRankService pageRankService;

	public PageRankMetric(ConceptSimilarityService simSvc,
			PageRankService pageRankService) {
		super(simSvc);
		this.pageRankService = pageRankService;
	}

	@Override
	public double similarity(String concept1, String concept2,
			Map<String, Double> conceptFilter, SimilarityInfo simInfo) {
		return pageRankService.sim(concept1, concept2,
				this.simSvc.getConceptGraph(), 10, 1e-2, 0.85);
	}

}
