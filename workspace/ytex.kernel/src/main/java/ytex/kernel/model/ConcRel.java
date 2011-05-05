package ytex.kernel.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConcRel implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ConcRel.class.getName());

	@Override
	public String toString() {
		return "ConcRel [nodeCUI=" + nodeCUI + "]";
	}

	/**
	 * id of this concept
	 */
	public String nodeCUI;
	/**
	 * parents of this concept
	 */
	public HashSet<ConcRel> parents;
	/**
	 * children of this concept
	 */
	public HashSet<ConcRel> children;
	/**
	 * for java object serialization, need to avoid default serializer behavior
	 * of writing out entire object graph. just write the parent/children object
	 * ids and resolve the connections after loading this object.
	 */
	public String[] parentsArray;
	public String[] childrenArray;

	public ConcRel(String cui) {
		nodeCUI = cui;
		parents = new HashSet<ConcRel>();
		children = new HashSet<ConcRel>();
		parentsArray = null;
		childrenArray = null;
	}

	/**
	 * recursively build all paths to root from a concept - add elements from
	 * set of parents.
	 * 
	 * @param lpath
	 *            current path from children to this concept
	 * @param allPaths
	 *            list of all paths
	 * @param depth
	 *            current depth
	 * @param depthMax
	 */
	public void getPath(List<ConcRel> lpath, List<List<ConcRel>> allPaths,
			int depth, int depthMax) {
		if (depth >= depthMax)
			return;
		if (lpath == null)
			lpath = new ArrayList<ConcRel>();

		lpath.add(this);

		if (isRoot()) {
			// add a copy to the list of all paths
			allPaths.add(new ArrayList<ConcRel>(lpath));
		} else {
			// recurse
			for (ConcRel p : parents) {
				p.getPath(lpath, allPaths, depth + 1, depthMax);
			}
		}
		lpath.remove(lpath.size() - 1);
	}

	/**
	 * get least common subsumer of the specified concepts and its distance from
	 * root
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static ObjPair<ConcRel, Integer> getLeastCommonConcept(ConcRel c1,
			ConcRel c2) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("getLeastCommonConcept(" + c1 + "," + c2 + ")");
		}
		// result
		ObjPair<ConcRel, Integer> res = new ObjPair<ConcRel, Integer>(null,
				Integer.MAX_VALUE);
		// concept 1's parent distance map
		Map<ConcRel, Integer> cand1 = new HashMap<ConcRel, Integer>();
		// concept 2's parent distance map
		Map<ConcRel, Integer> cand2 = new HashMap<ConcRel, Integer>();

		// parents of concept 1
		HashSet<ConcRel> parC1 = new HashSet<ConcRel>();
		parC1.add(c1);
		// parents of concept 2
		HashSet<ConcRel> parC2 = new HashSet<ConcRel>();
		parC2.add(c2);
		HashSet<ConcRel> tmp = new HashSet<ConcRel>();
		HashSet<ConcRel> tmp2;

		int dist = 0;
		// changed to start distance with 1 - we increment at the end of the
		// loop
		// we always look at the parents, so the distance has to start with 1
		// if one concept is the parent of the other, this would return 0 if
		// dist starts with 0
		// int dist = 1;
		// while there are parents
		// this does a dual-breadth first search
		// parC1 are the dist'th ancestors of concept 1
		// parC2 are the dist'th ancestors of concept 2
		while (!parC1.isEmpty() || !parC2.isEmpty()) {
			// grandparents
			tmp.clear();
			// go through parents of concept1
			for (Iterator<ConcRel> it = parC1.iterator(); it.hasNext();) {
				ConcRel cr = it.next();
				// checkif it's in the map concept2's parent distance map
				// - map of distances from concept 1
				if (cand2.containsKey(cr)) {
					res.v1 = cr;
					res.v2 = dist + cand2.get(cr).intValue();
					// return
					return res;
				}
				// not in the map - add it to the concept-distance map
				cand1.put(cr, dist);
				// add the grandparents to the tmp set
				tmp.addAll(cr.parents);
			}
			// remove concepts already in concept1's parent distance map from
			// the grandparent map
			tmp.removeAll(cand1.keySet());
			// tmp2 becomes the parents of c1
			tmp2 = parC1;
			// par c1 becomes grandparents minus parents
			parC1 = tmp;
			// tmp becomes tmp2, which is going to be killed in the next line
			tmp = tmp2;

			tmp.clear();
			// repeat everything for concept2 - go up one level
			for (Iterator<ConcRel> it = parC2.iterator(); it.hasNext();) {
				ConcRel cr = it.next();
				if (cand1.containsKey(cr)) {
					res.v1 = cr;
					res.v2 = dist + cand1.get(cr).intValue();
					return res;
				}
				cand2.put(cr, dist);
				tmp.addAll(cr.parents);
			}
			tmp.removeAll(cand2.keySet());
			tmp2 = parC2;
			parC2 = tmp;
			tmp = tmp2;

			++dist;
		}

		return res;
	}

	// public static ObjPair<ConcRel, Integer> getLeastCommonConcept(
	// Vector<Vector<ConcRel>> allPaths1, Vector<Vector<ConcRel>> allPaths2) {
	// ObjPair<ConcRel, Integer> res = new ObjPair<ConcRel, Integer>(null,
	// Integer.MAX_VALUE);
	// ObjPair<ConcRel, Integer> tmp = new ObjPair<ConcRel, Integer>(null,
	// Integer.MAX_VALUE);
	//
	// int n = 0;
	// for (Vector<ConcRel> path1 : allPaths1) {
	// // if(n++>200)
	// // break;
	// int n2 = 0;
	// for (Vector<ConcRel> path2 : allPaths2) {
	// // if(n2++>200)
	// // break;
	// if (getCommonConcept(path1, path2, tmp) != null) {
	// if (tmp.v2.intValue() < res.v2.intValue()) {
	// res.v1 = tmp.v1;
	// res.v2 = tmp.v2;
	// }
	// }
	// }
	// }
	//
	// return res;
	// }

	// public static ConcRel getCommonConcept(Vector<ConcRel> path1,
	// Vector<ConcRel> path2, ObjPair<ConcRel, Integer> oVals) {
	// ConcRel common = null;
	// int dist = Integer.MAX_VALUE;
	// int index1 = path1.size() - 1;
	// int index2 = path2.size() - 1;
	// while (index1 >= 0 && index2 >= 0) {
	// ConcRel r1 = path1.get(index1);
	// if (r1.equals(path2.get(index2))) {
	// common = r1;
	// dist = index1 + index2;
	// --index1;
	// --index2;
	// } else
	// break;
	// }
	//
	// oVals.v1 = common;
	// oVals.v2 = dist;
	//
	// return common;
	// }

	/**
	 * serialize parent/children concept ids, not the objects
	 */
	private void writeObject(java.io.ObjectOutputStream out)
			throws java.io.IOException {
		out.writeObject(nodeCUI);

		if (parentsArray == null) {
			parentsArray = new String[parents.size()];
			int i = 0;
			for (ConcRel c : parents)
				parentsArray[i++] = c.nodeCUI;
		}
		if (childrenArray == null) {
			childrenArray = new String[children.size()];
			int i = 0;
			for (ConcRel c : children)
				childrenArray[i++] = c.nodeCUI;
		}

		out.writeObject(parentsArray);
		out.writeObject(childrenArray);
		parentsArray = null;
		childrenArray = null;
	}

	/**
	 * read parent/children concept ids, not the objects
	 */
	private void readObject(java.io.ObjectInputStream in)
			throws java.io.IOException, ClassNotFoundException {
		nodeCUI = (String) in.readObject();
		parents = new HashSet<ConcRel>();
		children = new HashSet<ConcRel>();
		parentsArray = (String[]) in.readObject();
		childrenArray = (String[]) in.readObject();
	}

	/**
	 * reconstruct the relationships to other ConcRel objects
	 * 
	 * @param db
	 */
	public void constructRel(Map<String, ConcRel> db) {
		parents.clear();
		children.clear();
		for (String c : parentsArray)
			parents.add(db.get(c));

		parentsArray = null;

		for (String c : childrenArray)
			children.add(db.get(c));

		childrenArray = null;
	}

	/**
	 * is the specified concept an ancestor of this concept?
	 * 
	 * @param cui
	 * @return
	 */
	public boolean hasAncestor(String cui) {
		if (nodeCUI.equals(cui))
			return true;
		for (ConcRel c : parents) {
			if (c.hasAncestor(cui))
				return true;
		}
		return false;
	}

	public int depthMax() {
		int d = 0;
		for (Iterator<ConcRel> it = children.iterator(); it.hasNext();) {
			ConcRel child = it.next();
			int dm = child.depthMax() + 1;
			if (dm > d)
				d = dm;
		}
		return d;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public boolean isRoot() {
		return parents.isEmpty();
	}

	public int hashCode() {
		return nodeCUI.hashCode();
	}

	// public static void main(String[] args) {
	// int c1 = 18563; // 4903;
	// int c2 = 18670; // 175695;
	//
	// ConcRel r1 = MetaDB.concRelDB.cuiRelDB.get(c1);
	// ConcRel r2 = MetaDB.concRelDB.cuiRelDB.get(c2);
	// if (r1 == null)
	// System.out.println("No rel for " + c1);
	// if (r2 == null)
	// System.out.println("No rel for " + c2);
	//
	// if (r1 == null || r2 == null)
	// return;
	//
	// Vector<Vector<ConcRel>> allPaths1 = new Vector<Vector<ConcRel>>();
	// Vector<Vector<ConcRel>> allPaths2 = new Vector<Vector<ConcRel>>();
	//
	// r1.getPath(null, allPaths1, 0, 1000);
	// r2.getPath(null, allPaths2, 0, 1000);
	//
	// int i = 0;
	// System.out.println("***Paths for " + c1);
	// i = 0;
	// for (Vector<ConcRel> vc : allPaths1) {
	// System.out.print("#P" + (i++) + ": ");
	// i++;
	// for (ConcRel cr : vc) {
	// System.out.print("->" + cr.nodeCUI);
	// }
	// System.out.println("");
	// }
	//
	// System.out.println("***Paths for " + c2);
	// i = 0;
	// for (Vector<ConcRel> vc : allPaths2) {
	// System.out.print("##P" + (i++) + ": ");
	// for (ConcRel cr : vc) {
	// System.out.print("->" + cr.nodeCUI);
	// }
	// System.out.println("");
	// }
	//
	// ObjPair<ConcRel, Integer> obp = getLeastCommonConcept(allPaths1,
	// allPaths2);
	// System.out.println("Common concept :"
	// + (obp.v1 == null ? "none" : obp.v1.nodeCUI));
	// System.out.println("dist: " + obp.v2);
	//
	// obp = getLeastCommonConcept(r1, r2);
	// System.out.println("Common concept2 :"
	// + (obp.v1 == null ? "none" : obp.v1.nodeCUI));
	// System.out.println("dist: " + obp.v2);
	//
	// }
}
