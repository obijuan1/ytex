package ytex.web.search;

import ytex.kernel.metric.ConceptSimilarityService;

public class SemanticSimServiceBean {

	private ConceptSearchService conceptSearchService;
	private String description;
	private ConceptSimilarityService conceptSimilarityService;

	public ConceptSearchService getConceptSearchService() {
		return conceptSearchService;
	}

	public void setConceptSearchService(
			ConceptSearchService conceptSearchService) {
		this.conceptSearchService = conceptSearchService;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}
}
