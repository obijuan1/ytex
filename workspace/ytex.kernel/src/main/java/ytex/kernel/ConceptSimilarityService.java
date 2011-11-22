package ytex.kernel;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	/**
	 * supervised lin measure.
	 * 
	 * @param concept1
	 * @param concept2
	 * @param conceptFilter
	 *            map of concept id to imputed infogain. if the concept isn't in
	 *            this map, the concepts won't be compared. null for
	 *            unsupervised lin.
	 * @return
	 */
	public abstract double filteredLin(String concept1, String concept2,
			Map<String, Double> conceptFilter);

	/**
	 * list of tuis that corresponds to bitset indices
	 * 
	 * @return
	 */
	public abstract List<String> getTuiList();

	/**
	 * For the given label and cutoff, get the corresponding concepts whose
	 * propagated ig meets the threshold. Used by lin kernel to find concepts
	 * that actually have a non-trivial similarity
	 * 
	 * @param label
	 *            label
	 * @param rankCutoff
	 *            cutoff
	 * @param conceptFilter
	 *            set to fill with concepts
	 * @return double minimum evaluation
	 */
	public abstract double loadConceptFilter(String label, int rankCutoff,
			Map<String, Double> conceptFilter);

}