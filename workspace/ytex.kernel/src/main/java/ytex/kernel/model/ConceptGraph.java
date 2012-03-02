package ytex.kernel.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A directed graph that spans a subset of the UMLS connecting concepts with
 * IS-A links.
 * 
 * @author vijay
 */
public class ConceptGraph implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient List<ConcRel> conceptList = new ArrayList<ConcRel>();
	private Map<String, ConcRel> conceptMap = new HashMap<String, ConcRel>();
//	private int depthMax = 0;
	private String root = null;

	public List<ConcRel> getConceptList() {
		return conceptList;
	}

	public Map<String, ConcRel> getConceptMap() {
		return conceptMap;
	}

//	public int getDepthMax() {
//		return depthMax;
//	}

	public String getRoot() {
		return root;
	}

	public void setConceptList(List<ConcRel> conceptList) {
		this.conceptList = conceptList;
	}

	public void setConceptMap(Map<String, ConcRel> conceptMap) {
		this.conceptMap = conceptMap;
	}

//	public void setDepthMax(int depthMax) {
//		this.depthMax = depthMax;
//	}

	public void setRoot(String root) {
		this.root = root;
	}

	public ConcRel addConcept(String conceptID) {
		// get position at which concept would be added to list
		int nIndex = conceptList.size();
		// add concept to conceptMap
		ConcRel cr = new ConcRel(conceptID, nIndex);
		conceptMap.put(conceptID, cr);
		// add concept to list
		conceptList.add(cr);
		return cr;
	}

}
