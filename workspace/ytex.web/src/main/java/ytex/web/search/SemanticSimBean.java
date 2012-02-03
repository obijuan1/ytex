package ytex.web.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.event.ActionEvent;

import ytex.kernel.ConceptSimilarityService;
import ytex.kernel.ConceptSimilarityService.SimilarityMetricEnum;
import ytex.kernel.metric.SimilarityInfo;

public class SemanticSimBean {
	ConceptLookupBean concept1;
	ConceptLookupBean concept2;
	ConceptSimilarityService conceptSimilarityService;
	Map<String, String> lcsPathMap = new TreeMap<String, String>();
	Set<SimilarityMetricEnum> metrics = new HashSet<SimilarityMetricEnum>();
	Map<SimilarityMetricEnum, Double> similarityMap = new HashMap<SimilarityMetricEnum, Double>();
	SimilarityInfo simInfo = new SimilarityInfo();

	public SemanticSimBean() {
		metrics.add(SimilarityMetricEnum.INTRINSIC_LCH);
	}

	public ConceptLookupBean getConcept1() {
		return concept1;
	}

	public ConceptLookupBean getConcept2() {
		return concept2;
	}

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public Map<String, String> getLcsPathMap() {
		return lcsPathMap;
	}

	public Set<SimilarityMetricEnum> getMetrics() {
		return metrics;
	}

	public Map<SimilarityMetricEnum, Double> getSimilarityMap() {
		return similarityMap;
	}

	public SimilarityInfo getSimInfo() {
		return simInfo;
	}

	public void resetListen(ActionEvent event) {
		concept1.resetListen(event);
		concept2.resetListen(event);
	}

	public void setConcept1(ConceptLookupBean concept1) {
		this.concept1 = concept1;
	}

	public void setConcept2(ConceptLookupBean concept2) {
		this.concept2 = concept2;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	public void setLcsPathMap(Map<String, String> lcsPathMap) {
		this.lcsPathMap = lcsPathMap;
	}

	public void setMetrics(Set<SimilarityMetricEnum> metrics) {
		this.metrics = metrics;
	}

	public void setSimilarityMap(Map<SimilarityMetricEnum, Double> similarityMap) {
		this.similarityMap = similarityMap;
	}

	public void setSimInfo(SimilarityInfo simInfo) {
		this.simInfo = simInfo;
	}

	public void simListen(ActionEvent event) {
		if (this.concept1.getCurrentCUI() != null
				&& this.concept2.getCurrentCUI() != null) {
			this.concept1.setSearchCUI(this.concept1.getCurrentCUI());
			this.concept2.setSearchCUI(this.concept2.getCurrentCUI());
			this.simInfo = new SimilarityInfo();
			simInfo.setLcsPathMap(new HashMap<String, List<List<String>>>());
			this.similarityMap = this.conceptSimilarityService.similarity(
					metrics, concept1.getSearchCUI().getCui(), concept2
							.getSearchCUI().getCui(), null, simInfo);
			lcsPathMap.clear();
			if (simInfo.getLcsPathMap() != null) {
				for (Map.Entry<String, List<List<String>>> lcsPathEntry : simInfo
						.getLcsPathMap().entrySet()) {
					String lcs = lcsPathEntry.getKey();
					lcsPathMap.put(
							lcs,
							SimilarityInfo.formatLCSPath(lcs,
									lcsPathEntry.getValue()));
				}
			}
		}
	}

}
