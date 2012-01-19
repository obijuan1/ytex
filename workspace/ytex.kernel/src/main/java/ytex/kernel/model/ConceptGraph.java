package ytex.kernel.model;

import java.io.Serializable;
import java.util.HashMap;
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
	private Map<String, ConcRel> conceptMap = new HashMap<String, ConcRel>();
	private int depthMax = 0;
	private String root = null;

	public Map<String, ConcRel> getConceptMap() {
		return conceptMap;
	}

	public int getDepthMax() {
		return depthMax;
	}

	public String getRoot() {
		return root;
	}

	public void setConceptMap(Map<String, ConcRel> conceptMap) {
		this.conceptMap = conceptMap;
	}

	public void setDepthMax(int depthMax) {
		this.depthMax = depthMax;
	}

	public void setRoot(String root) {
		this.root = root;
	}
}
