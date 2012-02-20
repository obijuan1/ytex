package ytex.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import ytex.kernel.ConceptPair;

@WebService
public interface ConceptSimilarityWebService {
	public String getDefaultConceptGraph();
	
	public String[][] getConceptGraphs();

	/**
	 * compute similarity for a list of concept pairs
	 * 
	 * @param conceptGraphName optional
	 * @param conceptPairs
	 *            required, concept pairs for which similarity should be
	 *            computed
	 * @param metrics
	 *            required, similarity metrics to compute
	 * @param conceptFilter
	 *            optional - only lcs's in this set will be used.
	 * @param simInfos
	 *            optional - if provided, this list will be filled with the
	 *            similarity info for each concept pair.
	 * @return similarities
	 */
	@WebMethod
	public ConceptPairSimilarity[] similarities(@WebParam(name="conceptGraphName") String conceptGraphName, @WebParam(name="conceptPairs") ConceptPair[] conceptPairs,
			@WebParam(name="metrics") String[] metrics);
}
