package ytex.ws;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ytex.kernel.metric.ConceptPair;
import ytex.kernel.metric.ConceptPairSimilarity;

@WebService
public interface ConceptSimilarityWebService {
	public SimServiceInfo getDefaultConceptGraph();

	public List<SimServiceInfo> getConceptGraphs();

	/**
	 * compute similarity for a list of concept pairs
	 * 
	 * @param conceptGraphName optional
	 * @param conceptPairs
	 *            required, concept pairs for which similarity should be
	 *            computed
	 * @param metrics
	 *            required, similarity metrics to compute
	 * @param lcs
	 *            optional - if true, fill in the lcs paths for each concept pair.
	 * @return similarities
	 */
	@WebMethod
	public List<ConceptPairSimilarity> similarities(String conceptGraph,
			ConceptPair[] conceptPairs, String[] metrics, boolean lcs);
}
