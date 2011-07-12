package ytex.kernel;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ytex.kernel.model.ConceptGraph;

public interface ConceptSimilarityService {

	public abstract double lch(String concept1, String concept2);

	public abstract double lin(String concept1, String concept2);

	public int lcs(String concept1, String concept2, Map<String, List<List<String>>> lcsPath);

	public abstract ConceptGraph getConceptGraph();

	public abstract Map<String, Set<String>> getCuiTuiMap();

	public abstract double filteredLin(String concept1, String concept2, String label,
			double lcsMinEvaluation);

}