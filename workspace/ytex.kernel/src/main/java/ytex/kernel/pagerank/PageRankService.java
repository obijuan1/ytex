package ytex.kernel.pagerank;

import java.util.Map;

import ytex.kernel.model.ConceptGraph;

public interface PageRankService {

	/**
	 * PageRank for conceptGraph. Page = concept. in-links = parents. out-links
	 * = children.
	 * 
	 * @param dampingVector
	 *            topic vector/personalized pagerank vector. If null will use
	 *            normal pagerank with a damping vector where every value is 1/N
	 * @param cg
	 *            concept graph
	 * @param iter
	 *            max number of iterations
	 * @param threshold
	 *            convergence threshold
	 * @param dampingFactor
	 * @return pageRank 'vector'. key = concept (page), value = rank
	 */
	public abstract double[] rank(Map<String, Double> dampingVector,
			ConceptGraph cg, int iter, double threshold, double dampingFactor);

	/**
	 * call rank() with default values for iter (30), threshold(1e-4),
	 * dampingFactor(0.85)
	 * 
	 * @param dampingVector
	 * @param cg
	 * @return
	 */
	public abstract double[] rank(Map<String, Double> dampingVector,
			ConceptGraph cg);

	public abstract double sim(String concept1, String concept2, ConceptGraph cg,
			int iter, double threshold, double dampingFactor);

	public abstract double[] rank2(Map<Integer, Double> dampingVector, ConceptGraph cg, int iter,
			double threshold, double dampingFactor);

}