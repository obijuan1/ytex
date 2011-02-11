package ytex.kernel.model;

import java.io.Serializable;

public class ConceptPath implements Serializable {
	private int conceptPathId;
	private String cui;
	private String path[];

	public int getConceptPathId() {
		return conceptPathId;
	}

	public void setConceptPathId(int conceptPathId) {
		this.conceptPathId = conceptPathId;
	}

	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public String[] getPath() {
		return path;
	}

	public void setPath(String[] path) {
		this.path = path;
	}
}
