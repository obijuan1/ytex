package ytex.ws;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import ytex.kernel.ConceptPair;
import ytex.kernel.ConceptSimilarityService;
import ytex.kernel.ConceptSimilarityService.SimilarityMetricEnum;
import ytex.kernel.metric.LCSPath;
import ytex.kernel.metric.SimilarityInfo;
import ytex.web.search.SemanticSimRegistryBean;

@Path("/rest/")
@Produces("application/xml")
public class ConceptSimilarityRestServiceImpl {
	SemanticSimRegistryBean semanticSimRegistryBean;

	@GET
	@Path("/similarity")
	@Produces("application/xml")
	public ConceptPairSimilarity similarity(
			@QueryParam("conceptGraph") String conceptGraph,
			@QueryParam("concept1") String concept1,
			@QueryParam("concept2") String concept2,
			@QueryParam("metric") String metric) {
		String conceptGraphName = conceptGraph;
		if (conceptGraphName == null || conceptGraphName.length() == 0)
			conceptGraphName = semanticSimRegistryBean
					.getDefaultConceptGraphName();
		ConceptSimilarityService s = semanticSimRegistryBean
				.getSemanticSimServiceMap().get(conceptGraphName)
				.getConceptSimilarityService();
		if (s == null)
			throw new WebApplicationException(400);
		SimilarityMetricEnum m = SimilarityMetricEnum.PATH;
		if (metric == null || metric.length() == 0)
			m = SimilarityMetricEnum.valueOf(metric);
		if (m == null)
			throw new WebApplicationException(400);
		Set<SimilarityMetricEnum> metrics = new HashSet<SimilarityMetricEnum>(1);
		metrics.add(m);
		SimilarityInfo simInfo = new SimilarityInfo();
		simInfo.setLcsPaths(new ArrayList<LCSPath>(1));
		Map<SimilarityMetricEnum, Double> simMap = s.similarity(metrics,
				concept1, concept2, null, simInfo);
		ConceptPairSimilarity sim = new ConceptPairSimilarity();
		sim.setConceptPair(new ConceptPair(concept1, concept2));
		sim.setSimilarities(new ArrayList<Double>(simMap.values()));
		sim.setSimilarityInfo(simInfo);
		return sim;
	}
}
