package ytex.kernel.pagerank;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.KernelContextHolder;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;

public class PageRankServiceImpl implements PageRankService {
	private static final Log log = LogFactory.getLog(PageRankServiceImpl.class);

	@Override
	public Map<String, Double> rank(Map<String, Double> dampingVector,
			ConceptGraph cg) {
		return rank(dampingVector, cg, 30, 1e-4, 0.85);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.pagerank.PageRankService#rank(java.util.Map,
	 * ytex.kernel.model.ConceptGraph, int, double, double)
	 */
	@Override
	public Map<String, Double> rank(Map<String, Double> dampingVector,
			ConceptGraph cg, int iter, double threshold, double dampingFactor) {
		// initialize scoreMap with page scoring
		Map<Integer, Double> dvIndexMap = null;
		// convert from concept name to index
		if (dampingVector != null) {
			dvIndexMap = new HashMap<Integer, Double>(dampingVector.size());
			for (Map.Entry<String, Double> dvEntry : dampingVector.entrySet()) {
				dvIndexMap.put(cg.getConceptMap().get(dvEntry.getKey())
						.getNodeIndex(), dvEntry.getValue());
			}
		}
		Map<Integer, Double> scoreMapCurrent = rankInternal(dvIndexMap, cg,
				iter, threshold, dampingFactor);
		// convert from concept index to name
		Map<String, Double> prMap = new HashMap<String, Double>();
		for (Map.Entry<Integer, Double> scoreMapEntry : scoreMapCurrent
				.entrySet()) {
			prMap.put(cg.getConceptList().get(scoreMapEntry.getKey())
					.getConceptID(), scoreMapEntry.getValue());
		}
		return prMap;
	}

	private Map<Integer, Double> rankInternal(
			Map<Integer, Double> dampingVector, ConceptGraph cg, int iter,
			double threshold, double dampingFactor) {
		Map<Integer, Double> scoreMapCurrent = dampingVector;
		double N = (double) cg.getConceptMap().size();
		double diff = 1d;
		for (int i = 0; i < iter; i++) {
			Map<Integer, Double> scoreMapOld = scoreMapCurrent;
			scoreMapCurrent = pagerankIter(scoreMapCurrent, dampingVector, cg,
					dampingFactor, N);
			if (scoreMapOld != null
					&& (diff = difference(scoreMapCurrent, scoreMapOld)) <= threshold)
				break;
		}
		if (log.isDebugEnabled() && diff > threshold) {
			log.debug("did not converge, diff = " + diff + ", dampingVector = "
					+ dampingVector);
		}
		return scoreMapCurrent;
	}

	/**
	 * difference between 2 vectors
	 * 
	 * @param a
	 * @param b
	 * @return a-b
	 */
	private <T> double difference(Map<T, Double> a, Map<T, Double> b) {
		double diff = 0d;
		for (Map.Entry<T, Double> aiEntry : a.entrySet()) {
			Double bi = b.get(aiEntry.getKey());
			diff += Math.pow(
					aiEntry.getValue() - (bi != null ? bi.doubleValue() : 0d),
					2);
		}
		for (Map.Entry<T, Double> biEntry : b.entrySet()) {
			if (!a.containsKey(biEntry.getKey())) {
				diff += Math.pow(biEntry.getValue(), 2);
			}
		}
		return diff;
	}

	/**
	 * perform one iteration of pagerank
	 * 
	 * @param currentScores
	 * @param cg
	 * @return
	 */
	public Map<Integer, Double> pagerankIter(
			Map<Integer, Double> currentScores,
			Map<Integer, Double> dampingVector, ConceptGraph cg,
			double dampingFactor, double N) {
		Map<Integer, Double> newScores = new HashMap<Integer, Double>();
		if (dampingVector == null) {
			// the constant probability of randomly surfing into this node,
			// adjusted by damping factor
			double jump = ((1 - dampingFactor) / N);
			double initialValue = 1 / N;
			// the basic pagerank iteration with uniform damping vector
			// iterate over all nodes
			for (ConcRel c : cg.getConceptList()) {
				double score = 0d;
				// get nodes pointing at node c
				for (ConcRel in : c.getParents()) {
					// get the pagerank for node p which is pointing at c
					// if this is the first iteration, currentScores is null so
					// use the initial pagerank
					double prIn = currentScores == null ? initialValue
							: currentScores.get(in.getNodeIndex());
					// add the pagerank divided by the number of nodes p is
					// pointing at
					score += (prIn / (double) in.getChildren().size());
				}
				// adjust for uniform damping
				double adjusted = (score * dampingFactor) + jump;
				newScores.put(c.getNodeIndex(), adjusted);
			}
			// for (ConcRel c : cg.getConceptMap().values()) {
			// double score = 0d;
			// // get nodes pointing at node c
			// for (ConcRel in : c.getParents()) {
			// // get the pagerank for node p which is pointing at c
			// // if this is the first iteration, currentScores is null so
			// // use the initial pagerank
			// double prIn = currentScores == null ? initialValue
			// : currentScores.get(in.getConceptID());
			// // add the pagerank divided by the number of nodes p is
			// // pointing at
			// score += (prIn / (double) in.getChildren().size());
			// }
			// // adjust for uniform damping
			// double adjusted = (score * dampingFactor) + jump;
			// newScores.put(c.getConceptID(), adjusted);
			// }
		} else {
			// pagerank with non-uniform damping vector (topic vector).
			// because of the non-uniform damping vector, few nodes will have a
			// non-zero pagerank.
			// optimized so that we only iterate over nodes with non-zero
			// pagerank.
			// propagate from non-zero nodes to linked nodes
			// we assume currentScores is non-null - it is initialized to the
			// damping vector.
			// iterate over nodes that have a pagerank, and propagate the
			// pagerank to out-links.
			for (Map.Entry<Integer, Double> scoreEntry : currentScores
					.entrySet()) {
				// page (concept id)
				Integer index = scoreEntry.getKey();
				// pagerank
				double score = scoreEntry.getValue();
				// get concept id
				ConcRel cr = cg.getConceptList().get(index);
				// get number of out-links
				double nOutlinks = (double) cr.getChildren().size();
				if (nOutlinks > 0) {
					// propagate pagerank to out-links (children)
					for (ConcRel crOut : cr.getChildren()) {
						// get current pagerank value for target page
						double childScore = 0d;
						Double childScoreD = newScores
								.get(crOut.getNodeIndex());
						if (childScoreD != null)
							childScore = childScoreD.doubleValue();
						// add the pagerank/|links|
						childScore += (score / nOutlinks);
						newScores.put(crOut.getNodeIndex(), childScore);
					}
				}
			}
			// we just added the contribution of pages to newScores sum(score).
			// adjust: convert to (d)*sum(score) + (1-d)*v_i
			for (Map.Entry<Integer, Double> scoreEntry : newScores.entrySet()) {
				// v_i
				Double v_i = dampingVector.get(scoreEntry.getKey());
				// 1-c * v_i
				double v_i_adj = v_i != null ? v_i * (1 - dampingFactor) : 0d;
				double adjusted = (scoreEntry.getValue() * dampingFactor)
						+ v_i_adj;
				scoreEntry.setValue(adjusted);
			}
			//
			//
			// for (Map.Entry<String, Double> scoreEntry : currentScores
			// .entrySet()) {
			// // page (concept id)
			// String page = scoreEntry.getKey();
			// // pagerank
			// double score = scoreEntry.getValue();
			// // get concept id
			// ConcRel cr = cg.getConceptMap().get(page);
			// // get number of out-links
			// double nOutlinks = (double) cr.getChildren().size();
			// if (nOutlinks > 0) {
			// // propagate pagerank to out-links (children)
			// for (ConcRel crOut : cr.getChildren()) {
			// // get current pagerank value for target page
			// double childScore = 0d;
			// Double childScoreD = newScores
			// .get(crOut.getConceptID());
			// if (childScoreD != null)
			// childScore = childScoreD.doubleValue();
			// // add the pagerank/|links|
			// childScore += (score / nOutlinks);
			// newScores.put(crOut.getConceptID(), childScore);
			// }
			// }
			// }
			// // we just added the contribution of pages to newScores
			// sum(score).
			// // adjust: convert to (d)*sum(score) + (1-d)*v_i
			// for (Map.Entry<String, Double> scoreEntry : newScores.entrySet())
			// {
			// // v_i
			// Double v_i = dampingVector.get(scoreEntry.getKey());
			// // 1-c * v_i
			// double v_i_adj = v_i != null ? v_i * (1 - dampingFactor) : 0d;
			// double adjusted = (scoreEntry.getValue() * dampingFactor)
			// + v_i_adj;
			// scoreEntry.setValue(adjusted);
			// }
		}
		return newScores;
	}

	/**
	 * compute similarity using personalized page rank, as documented in <a
	 * href=
	 * "http://ixa.si.ehu.es/Ixa/Argitalpenak/Artikuluak/1274099085/publikoak/main.pdf"
	 * >Exploring Knowledge Bases for Similarity</a>
	 * 
	 * @param concept1
	 * @param concept2
	 * @param cg
	 * @param iter
	 * @param threshold
	 * @param dampingFactor
	 * @return
	 */
	@Override
	public double sim(String concept1, String concept2, ConceptGraph cg,
			int iter, double threshold, double dampingFactor) {
		Map<Integer, Double> c1dv = new HashMap<Integer, Double>(1);
		c1dv.put(cg.getConceptMap().get(concept1).getNodeIndex(), 1d);
		Map<Integer, Double> c1pr = this.rankInternal(c1dv, cg, iter,
				threshold, dampingFactor);
		Map<Integer, Double> c2dv = new HashMap<Integer, Double>(1);
		c2dv.put(cg.getConceptMap().get(concept2).getNodeIndex(), 1d);
		Map<Integer, Double> c2pr = this.rankInternal(c2dv, cg, iter,
				threshold, dampingFactor);
		return cosine(c1pr, c2pr);
	}

	/**
	 * cosine of two vectors
	 * 
	 * @param u
	 * @param v
	 * @return
	 */
	private <T> double cosine(Map<T, Double> u, Map<T, Double> v) {
		double uu = 0d;
		double uv = 0d;
		double vv = 0d;
		if (u.isEmpty() || v.isEmpty())
			return 0d;
		// in this loop compute u*u, and u*v
		for (Map.Entry<T, Double> uEntry : u.entrySet()) {
			double ui = uEntry.getValue();
			T uC = uEntry.getKey();
			uu += ui * ui;
			Double vi = v.get(uC);
			if (vi != null)
				uv += ui * vi.doubleValue();
		}
		if (uv == 0)
			return 0d;
		// in this loop, compute v*v
		for (double vi : v.values()) {
			vv += vi * vi;
		}
		// u*v/sqrt(v*v)*sqrt(u*u)
		return uv / Math.sqrt(vv * uu);
	}

	public static void main(String args[]) {
		Options options = new Options();
		OptionGroup og = new OptionGroup();
		og.addOption(OptionBuilder
				.withArgName("concept1,concept2")
				.hasArg()
				.withDescription(
						"compute similarity for specified concept pair")
				.create("sim"));
		og.addOption(OptionBuilder
				.withArgName("concept1,concept2,...")
				.hasArg()
				.withDescription(
						"personalized pagerank vector for specified concepts ")
				.create("ppr"));
		og.setRequired(true);
		options.addOptionGroup(og);
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);
			Properties ytexProps = new Properties();
			ytexProps.putAll((Properties) KernelContextHolder
					.getApplicationContext().getBean("ytexProperties"));
			ytexProps.putAll(System.getProperties());
			ConceptDao conceptDao = KernelContextHolder.getApplicationContext()
					.getBean(ConceptDao.class);
			PageRankService pageRankService = KernelContextHolder
					.getApplicationContext().getBean(PageRankService.class);
			ConceptGraph cg = conceptDao.getConceptGraph(ytexProps
					.getProperty("ytex.conceptGraphName"));
			if (line.hasOption("sim")) {
				String cs = line.getOptionValue("sim");
				String concept[] = cs.split(",");
				System.out.println(pageRankService.sim(concept[0], concept[1],
						cg, 30, 1e-4, 0.85));
			} else if (line.hasOption("ppr")) {
				String cs = line.getOptionValue("ppr");
				String concept[] = cs.split(",");
				double weight = 1 / (double) concept.length;
				Map<String, Double> ppv = new HashMap<String, Double>();
				for (String c : concept) {
					ppv.put(c, weight);
				}
				System.out.println(pageRankService.rank(ppv, cg));
			}
		} catch (ParseException pe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter
					.printHelp(
							"java "
									+ PageRankServiceImpl.class.getName()
									+ " compute personalized page rank or similarity.  used for testing purposes",
							options);
		}

	}
}
