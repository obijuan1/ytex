package ytex.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.ws.rs.WebApplicationException;

import ytex.kernel.ConceptPair;
import ytex.kernel.ConceptSimilarityService;
import ytex.kernel.ConceptSimilarityService.SimilarityMetricEnum;
import ytex.kernel.metric.LCSPath;
import ytex.kernel.metric.SimilarityInfo;
import ytex.web.search.SemanticSimRegistryBean;

@WebService(endpointInterface = "ytex.ws.ConceptSimilarityWebService")
public class ConceptSimilarityWebServiceImpl implements
		ConceptSimilarityWebService, ConceptSimilarityRestService {
	public SemanticSimRegistryBean getSemanticSimRegistryBean() {
		return semanticSimRegistryBean;
	}

	public void setSemanticSimRegistryBean(
			SemanticSimRegistryBean semanticSimRegistryBean) {
		this.semanticSimRegistryBean = semanticSimRegistryBean;
	}

	SemanticSimRegistryBean semanticSimRegistryBean;

	public ConceptPairSimilarity similarity(String conceptGraph,
			String concept1, String concept2, String metrics) {
		ConceptSimilarityService s = getConceptSimilarityService(conceptGraph);
		if (s == null)
			throw new WebApplicationException(400);
		Map<SimilarityMetricEnum, Integer> metricIndexMap = this
				.metricArrayToMap(metrics.split(","));
		SimilarityInfo simInfo = new SimilarityInfo();
		simInfo.setLcsPaths(new ArrayList<LCSPath>(1));
		Map<SimilarityMetricEnum, Double> sim = s.similarity(
				metricIndexMap.keySet(), concept1, concept2, null, simInfo);
		ConceptPairSimilarity conceptPairSim = new ConceptPairSimilarity();
		fillConceptPairSim(metricIndexMap, sim, conceptPairSim);
		conceptPairSim.setSimilarityInfo(simInfo);
		conceptPairSim.setConceptPair(new ConceptPair(concept1, concept2));
		return conceptPairSim;
	}

	private ConceptSimilarityService getConceptSimilarityService(
			String conceptGraph) {
		String conceptGraphName = conceptGraph;
		if (conceptGraphName == null || conceptGraphName.length() == 0)
			conceptGraphName = semanticSimRegistryBean
					.getDefaultConceptGraphName();
		ConceptSimilarityService s = semanticSimRegistryBean
				.getSemanticSimServiceMap().get(conceptGraphName)
				.getConceptSimilarityService();
		return s;
	}

	public ConceptPairSimilarity[] similarities(String conceptGraph,
			ConceptPair[] conceptPairs, String[] metrics) {
		ConceptSimilarityService s = this
				.getConceptSimilarityService(conceptGraph);
		Map<SimilarityMetricEnum, Integer> metricIndexMap = metricArrayToMap(metrics);
		ConceptPairSimilarity[] sims = new ConceptPairSimilarity[conceptPairs.length];
		List<ConceptPair> conceptPairList = Arrays.asList(conceptPairs);
		List<SimilarityInfo> simInfoList = new ArrayList<SimilarityInfo>(
				conceptPairList.size());
		List<Map<SimilarityMetricEnum, Double>> simList = s.similarity(
				conceptPairList, metricIndexMap.keySet(), null, simInfoList);
		int row = 0;
		for (Map<SimilarityMetricEnum, Double> sim : simList) {
			ConceptPairSimilarity conceptPairSim = new ConceptPairSimilarity();
			fillConceptPairSim(metricIndexMap, sim, conceptPairSim);
			conceptPairSim.setSimilarityInfo(simInfoList.get(row));
			conceptPairSim.setConceptPair(conceptPairs[row]);
			sims[row] = conceptPairSim;
			row++;
		}
		return sims;
	}

	private Map<SimilarityMetricEnum, Integer> metricArrayToMap(String[] metrics) {
		Map<SimilarityMetricEnum, Integer> metricIndexMap = new HashMap<SimilarityMetricEnum, Integer>();
		int index = 0;
		for (String metric : metrics) {
			SimilarityMetricEnum m = SimilarityMetricEnum.valueOf(metric);
			if (m != null)
				metricIndexMap.put(m, index++);

		}
		return metricIndexMap;
	}

	private void fillConceptPairSim(
			Map<SimilarityMetricEnum, Integer> metricIndexMap,
			Map<SimilarityMetricEnum, Double> sim,
			ConceptPairSimilarity conceptPairSim) {
		Double simMetricVals[] = new Double[metricIndexMap.size()];
		for (Map.Entry<SimilarityMetricEnum, Double> simEntry : sim.entrySet()) {
			simMetricVals[metricIndexMap.get(simEntry.getKey())] = simEntry
					.getValue();
		}
		conceptPairSim.setSimilarities(Arrays.asList(simMetricVals));
	}

	public String getDefaultConceptGraph() {
		return this.semanticSimRegistryBean.getDefaultConceptGraphName();
	}

	public String[][] getConceptGraphs() {
		String[][] cgs = new String[semanticSimRegistryBean
				.getSemanticSimDescriptionMap().size()][2];
		int i = 0;
		for (Map.Entry<String, String> entry : semanticSimRegistryBean
				.getSemanticSimDescriptionMap().entrySet()) {
			cgs[i][0] = entry.getKey();
			cgs[i][1] = entry.getValue();
			i++;
		}
		return cgs;
	}

}
