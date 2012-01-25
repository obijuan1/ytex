package ytex.kernel;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ytex.kernel.metric.SimilarityInfo;
import ytex.kernel.model.ConceptGraph;

public interface ConceptSimilarityService {

	public enum SimilarityMetricEnum {
		LCH, INTRINSIC_LCH, LIN, INTRINSIC_LIN, PATH, INTRINSIC_PATH, JACCARD, SOKAL, PAGERANK
	}

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

	/**
	 * get the lcs(s) for the specified concepts
	 * 
	 * @param concept1
	 *            required
	 * @param concept2
	 *            required
	 * @param lcses
	 *            required - will be filled with the lcs(s).
	 * @param lcsPathMap
	 *            optional - will be filled with lcs and paths through the
	 *            lcses.
	 * @return distance of path through lcs
	 */
	public int getLCS(String concept1, String concept2, Set<String> lcses,
			Map<String, List<List<String>>> lcsPathMap);

	/**
	 * get the best lcs
	 * 
	 * @param lcses
	 *            set of lcses
	 * @param intrinsicIC
	 *            should the intrinsic ic be used? false - use corpus-based ic.
	 *            For multiple lcses not using concept filter, use the lcs with
	 *            the lowest infocontent
	 * @param conceptFilter
	 *            limit to lcses in the concept filter. The lcs with the highest
	 *            value will be used.
	 * @return array with 2 entries. Entry 1 - lcs (String). Entry 2 -
	 *         infocontent (double). Null if no lcses are in the concept filter.
	 */
	public Object[] getBestLCS(Set<String> lcses, boolean intrinsicIC,
			Map<String, Double> conceptFilter);

	public abstract double getIC(String concept, boolean intrinsicICMap);

	/**
	 * compute similarity for a pair of concepts
	 * 
	 * @param metrics
	 *            required, similarity metrics to compute
	 * @param concept1
	 *            required
	 * @param concept2
	 *            required
	 * @param conceptFilter
	 *            optional - only lcs's in this set will be used.
	 * @param simInfo
	 *            optional - pass this to get information on lcs. Instantiate
	 *            the lcsPathMap to get paths through lcs
	 * @return similarities
	 */
	public abstract Map<SimilarityMetricEnum, Double> similarity(
			Set<SimilarityMetricEnum> metrics, String concept1,
			String concept2, Map<String, Double> conceptFilter,
			SimilarityInfo simInfo);

	/**
	 * compute similarity for a list of concept pairs
	 * 
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
	public List<Map<SimilarityMetricEnum, Double>> similarity(
			List<ConceptPair> conceptPairs, Set<SimilarityMetricEnum> metrics,
			Map<String, Double> conceptFilter, List<SimilarityInfo> simInfos);
}