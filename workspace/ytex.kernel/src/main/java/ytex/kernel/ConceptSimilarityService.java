package ytex.kernel;

import ytex.kernel.model.ConceptGraph;

public interface ConceptSimilarityService {

	public abstract double lch(String concept1, String concept2);

	public abstract double lin(String corpus, String concept1, String concept2);

	public void updateInformationContent(String corpusName);

	public Object[] lcs(String concept1, String concept2);

	public abstract ConceptGraph getConceptGraph();

}