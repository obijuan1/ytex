package ytex.kernel.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	public static String SAB_ALL = "UMLS_ALL";
	private int conceptGraphId;
	private Map<String, ConcRel> conceptMap = new HashMap<String, ConcRel>();
	private Set<String> roots = new HashSet<String>();
	private int depthMax = 0;
	private String[] sourceVocabularies;

	public String[] getSourceVocabularies() {
		return sourceVocabularies;
	}

	public void setSourceVocabularies(String[] sourceVocabularies) {
		this.sourceVocabularies = sourceVocabularies;
	}

	public Set<String> getRoots() {
		return roots;
	}

	public void setRoots(Set<String> roots) {
		this.roots = roots;
	}

	public Map<String, ConcRel> getConceptMap() {
		return conceptMap;
	}

	public void setConceptMap(Map<String, ConcRel> conceptMap) {
		this.conceptMap = conceptMap;
	}

	public int getConceptGraphId() {
		return conceptGraphId;
	}

	public void setConceptGraphId(int conceptGraphId) {
		this.conceptGraphId = conceptGraphId;
	}

	public int getDepthMax() {
		return depthMax;
	}

	public void setDepthMax(int depthMax) {
		this.depthMax = depthMax;
	}
}
