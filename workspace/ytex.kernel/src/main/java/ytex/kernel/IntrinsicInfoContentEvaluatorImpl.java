package ytex.kernel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureRank;

public class IntrinsicInfoContentEvaluatorImpl implements
		IntrinsicInfoContentEvaluator {
	public static class IntrinsicICInfo {
		private ConcRel concept;

		private int leafCount = 0;

		private int subsumerCount = 0;

		public IntrinsicICInfo(ConcRel concept) {
			this.concept = concept;
		}

		public ConcRel getConcept() {
			return concept;
		}

		public int getLeafCount() {
			return leafCount;
		}

		public int getSubsumerCount() {
			return subsumerCount;
		}

		public void setConcept(ConcRel concept) {
			this.concept = concept;
		}

		public void setLeafCount(int leafCount) {
			this.leafCount = leafCount;
		}

		public void setSubsumerCount(int subsumerCount) {
			this.subsumerCount = subsumerCount;
		}
	}

	private static final Log log = LogFactory
			.getLog(IntrinsicInfoContentEvaluatorImpl.class);
	private static final double log2adjust = 1d / Math.log(2);

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Properties props = (Properties) KernelContextHolder
				.getApplicationContext().getBean("ytexProperties");
		props.putAll(System.getProperties());
		if (!props.containsKey("ytex.conceptGraphName")) {
			System.err.println("error: ytex.conceptGraphName not specified");
			System.exit(1);
		} else {
			IntrinsicInfoContentEvaluator corpusEvaluator = KernelContextHolder
					.getApplicationContext().getBean(
							IntrinsicInfoContentEvaluator.class);
			corpusEvaluator.evaluateIntrinsicInfoContent(props);
			System.exit(0);
		}
	}

	private ClassifierEvaluationDao classifierEvaluationDao;

	private ConceptDao conceptDao;

	private double computeIC(IntrinsicICInfo icInfo, int maxLeaves) {
		// |leaves(c)|/|subsumers(c)| + 1
		double denom = log2adjust
				* Math.log((double) icInfo.getLeafCount()
						/ (double) icInfo.getSubsumerCount() + 1d);
		// max_leaves + 1
		double num = log2adjust * Math.log((double) maxLeaves + 1d);
		return num - denom;
	}

	/**
	 * add/update icInfoMap entry for concept with the concept's leaf count
	 * 
	 * @param concept
	 * @param icInfoMap
	 * @param w
	 * @param subsumerMap
	 * @throws IOException
	 */
	private void computeLeafCount(ConcRel concept,
			Map<String, IntrinsicICInfo> icInfoMap,
			Map<String, Set<String>> leafMap, BufferedWriter w)
			throws IOException {
		// see if we already computed this
		IntrinsicICInfo icInfo = icInfoMap.get(concept.getConceptID());
		if (icInfo != null && icInfo.getLeafCount() > 0) {
			return;
		}
		// if not, figure it out
		if (icInfo == null) {
			icInfo = new IntrinsicICInfo(concept);
			icInfoMap.put(concept.getConceptID(), icInfo);
		}
		// for leaves the default (0) is correct
		if (!concept.isLeaf()) {
			Set<String> leaves = this.getLeaves(concept, leafMap);
			icInfo.setLeafCount(leaves.size());
			if (w != null) {
				w.write(concept.getConceptID());
				w.write("\t");
				w.write(Integer.toString(leaves.size()));
				w.write("\t");
				w.write(leaves.toString());
				w.newLine();
			}
		}
		// recurse to parents
		for (ConcRel parent : concept.getParents()) {
			computeLeafCount(parent, icInfoMap, leafMap, w);
		}
	}

	/**
	 * add/update icInfoMap entry for concept with the concept's subsumer count
	 * 
	 * @param concept
	 * @param icInfoMap
	 * @param subsumerMap
	 * @param w
	 * @throws IOException
	 */
	private void computeSubsumerCount(ConcRel concept,
			Map<String, IntrinsicICInfo> icInfoMap,
			Map<String, Set<String>> subsumerMap, BufferedWriter w)
			throws IOException {
		// see if we already computed this
		IntrinsicICInfo icInfo = icInfoMap.get(concept.getConceptID());
		if (icInfo != null && icInfo.getSubsumerCount() > 0) {
			return;
		}
		// if not, figure it out
		if (icInfo == null) {
			icInfo = new IntrinsicICInfo(concept);
			icInfoMap.put(concept.getConceptID(), icInfo);
		}
		Set<String> subsumers = this.getSubsumers(concept, subsumerMap);
		if (w != null) {
			w.write(concept.getConceptID());
			w.write("\t");
			w.write(Integer.toString(subsumers.size()));
			w.write("\t");
			w.write(subsumers.toString());
			w.newLine();
		}
		icInfo.setSubsumerCount(subsumers.size());
		// recursively compute the children's subsumer counts
		for (ConcRel child : concept.getChildren()) {
			computeSubsumerCount(child, icInfoMap, subsumerMap, w);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.IntrinsicInfoContentEvaluator#evaluateIntrinsicInfoContent
	 * (java.lang.String)
	 */
	@Override
	public void evaluateIntrinsicInfoContent(final Properties props)
			throws IOException {
		String conceptGraphName = props.getProperty("ytex.conceptGraphName");
		String conceptGraphDir = props.getProperty("ytex.conceptGraphDir",
				System.getProperty("java.io.tmpdir"));
		ConceptGraph cg = this.conceptDao.getConceptGraph(conceptGraphName);
		log.info("computing max leaves");
		log.info("computing subsumer counts");
		// compute the subsumer count
		Map<String, IntrinsicICInfo> icInfoMap = new HashMap<String, IntrinsicICInfo>();
		Map<String, Set<String>> subsumerMap = new WeakHashMap<String, Set<String>>();
		BufferedWriter w = null;
		try {
			w = this.getOutputFile(conceptGraphName, conceptGraphDir,
					"subsumer");
			computeSubsumerCount(cg.getConceptMap().get(cg.getRoot()),
					icInfoMap, subsumerMap, w);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
				}
			}
		}
		subsumerMap = null;
		// get the leaves in this concept graph
		Set<String> leafSet = null;
		try {
			w = this.getOutputFile(conceptGraphName, conceptGraphDir, "allleaf");
			leafSet = this.getAllLeaves(cg, w);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
				}
			}
		}
		log.info("computing leaf counts");
		Map<String, Set<String>> leafMap = new WeakHashMap<String, Set<String>>();
		// compute leaf count of all concepts in this graph
		try {
			w = this.getOutputFile(conceptGraphName, conceptGraphDir, "leaf");
			for (String leaf : leafSet) {
				computeLeafCount(cg.getConceptMap().get(leaf), icInfoMap,
						leafMap, w);
			}
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
				}
			}
		}
		leafMap = null;
		log.info("storing intrinsic ic");
		storeIntrinsicIC(conceptGraphName, leafSet.size(), icInfoMap);
	}

	private BufferedWriter getOutputFile(final String conceptGraphName,
			final String conceptGraphDir, String type) throws IOException {
		return new BufferedWriter(new FileWriter(FileUtil.addFilenameToDir(
				conceptGraphDir, conceptGraphName + "-" + type + ".txt")));
	}

	public Set<String> getAllLeaves(ConceptGraph cg, BufferedWriter w)
			throws IOException {
		Set<String> leafSet = new HashSet<String>();
		for (Map.Entry<String, ConcRel> con : cg.getConceptMap().entrySet()) {
			if (con.getValue().isLeaf()) {
				leafSet.add(con.getValue().getConceptID());
			}
		}
		if (w != null) {
			w.write(Integer.toString(leafSet.size()));
			w.write("\t");
			w.write(leafSet.toString());
			w.newLine();
		}
		return leafSet;
	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public ConceptDao getConceptDao() {
		return conceptDao;
	}

	private Set<String> getLeaves(ConcRel concept,
			Map<String, Set<String>> leafMap) {
		// look in cache
		if (leafMap.containsKey(concept.getConceptID()))
			return leafMap.get(concept.getConceptID());
		// not in cache - compute recursively
		Set<String> leaves = new HashSet<String>();
		if (concept.isLeaf()) {
			// for leaves, just add the concept id
			leaves.add(concept.getConceptID());
		} else {
			// for inner nodes, recurse
			for (ConcRel child : concept.getChildren()) {
				leaves.addAll(getLeaves(child, leafMap));
			}
		}
		// add this to the cache - copy the key so that it can be gc'ed
		leafMap.put(new String(concept.getConceptID()), leaves);
		return leaves;
	}

	/**
	 * recursively compute the subsumers of a concept
	 * 
	 * @param concept
	 * @param subsumerMap
	 * @return
	 */
	private Set<String> getSubsumers(ConcRel concept,
			Map<String, Set<String>> subsumerMap) {
		// look in cache
		if (subsumerMap.containsKey(concept.getConceptID()))
			return subsumerMap.get(concept.getConceptID());
		// not in cache - compute recursively
		Set<String> subsumers = new HashSet<String>();
		// no parents - add the fake root
		if (concept.getParents() != null && !concept.getParents().isEmpty()) {
			// parents - recurse
			for (ConcRel parent : concept.getParents()) {
				subsumers.addAll(getSubsumers(parent, subsumerMap));
			}
		}
		// add the concept itself to the set of subsumers
		subsumers.add(concept.getConceptID());
		// add this to the cache - copy the key so that this can be gc'ed as
		// needed
		subsumerMap.put(new String(concept.getConceptID()), subsumers);
		return subsumers;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public void setConceptDao(ConceptDao conceptDao) {
		this.conceptDao = conceptDao;
	}

	private void storeIntrinsicIC(String conceptGraphName, int maxLeaves,
			Map<String, IntrinsicICInfo> icInfoMap) {
		FeatureEvaluation fe = new FeatureEvaluation();
		fe.setEvaluationType("intrinsic-infocontent");
		fe.setParam2(conceptGraphName);
		List<FeatureRank> listFeatureRank = new ArrayList<FeatureRank>(
				icInfoMap.size());
		for (IntrinsicICInfo icInfo : icInfoMap.values()) {
			double ic = computeIC(icInfo, maxLeaves);
			listFeatureRank.add(new FeatureRank(fe, icInfo.getConcept()
					.getConceptID(), ic));
		}
		this.classifierEvaluationDao.deleteFeatureEvaluation(null, null, null,
				fe.getEvaluationType(), null, 0d, conceptGraphName);
		this.classifierEvaluationDao.saveFeatureEvaluation(fe, listFeatureRank);
	}
}
