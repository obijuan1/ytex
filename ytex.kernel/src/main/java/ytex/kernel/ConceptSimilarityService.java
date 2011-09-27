package ytex.kernel;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import ytex.kernel.model.ConceptGraph;

public interface ConceptSimilarityService {

	public abstract double lch(String concept1, String concept2);

	public abstract double lin(String concept1, String concept2);

	public int lcs(String concept1, String concept2,
			Map<String, List<List<String>>> lcsPath);

	public abstract ConceptGraph getConceptGraph();

	/**
	 * cui - tui map. tuis are bitsets, indices correspond to tuis in
	 * {@link #getTuiList()}
	 * 
	 * @return
	 */
	public abstract Map<String, BitSet> getCuiTuiMap();

	public abstract double filteredLin(String concept1, String concept2,
			String label, double lcsMinEvaluation);

	/**
	 * list of tuis that corresponds to bitset indices
	 * 
	 * @return
	 */
	public abstract List<String> getTuiList();

}