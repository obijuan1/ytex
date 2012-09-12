package ytex;

import ytex.kernel.KernelContextHolder;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;

/**
 * print graph statistics
 * 
 * @author vijay
 * 
 */
public class GraphStats {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConceptDao cd = KernelContextHolder.getApplicationContext().getBean(
				ConceptDao.class);
		String cgNames[];
		if (args.length == 1) {
			cgNames = args[0].split(",");
		} else {
			cgNames = new String[] { System
					.getProperty("ytex.conceptGraphName") };
		}
		for (String cgName : cgNames) {
			printStats(cd, cgName);
		}
	}

	private static void printStats(ConceptDao cd, String cgName) {
		ConceptGraph cg = cd.getConceptGraph(cgName);
		if (cg == null) {
			System.out.println(cgName + ": not valid");
			return;
		}
		if (cg.getDepthMax() == 0) {
			// undirected graph
			int nEdges = 0;
			for (ConcRel cr : cg.getConceptList()) {
				nEdges += cr.getParentsArray().length;
			}
			System.out.println(cgName + ": edges=" + nEdges + ", vertices="
					+ cg.getConceptList().size());
		} else {
			int nEdges = 0;
			for (ConcRel cr : cg.getConceptList()) {
				nEdges += cr.getParents().size();
			}
			int rootDirectChildren = cg.getConceptMap().get(cg.getRoot())
					.getChildren().size();
			System.out.println(cgName + ": edges=" + nEdges + ", vertices="
					+ cg.getConceptList().size() + ", depthMax="
					+ cg.getDepthMax() + ", intrinsicICMax="
					+ cg.getIntrinsicICMax() + ", root=" + cg.getRoot()
					+ ", rootDirectChildren=" + rootDirectChildren);
		}
	}
}
