package ytex.ws;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import ytex.kernel.metric.ConceptPairSimilarity;

@Path("/rest/")
@Produces("application/xml")
public interface ConceptSimilarityRestService {
	
	@GET
	@Path("/getDefaultConceptGraph")
	public SimServiceInfo getDefaultConceptGraph();
	
	@GET
	@Path("/getConceptGraphs")
	public List<SimServiceInfo> getConceptGraphs();


	@GET
	@Path("/similarity")
	public ConceptPairSimilarity similarity(
			@QueryParam("conceptGraph") String conceptGraph,
			@QueryParam("concept1") String concept1,
			@QueryParam("concept2") String concept2,
			@QueryParam("metrics") String metrics,
			@QueryParam("lcs") String lcs);
}
