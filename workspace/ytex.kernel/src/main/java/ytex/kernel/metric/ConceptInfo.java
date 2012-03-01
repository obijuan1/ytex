package ytex.kernel.metric;

public class ConceptInfo {
	private String conceptId;
	private int depth;
	private double corpusIC;
	private double intrinsicIC;

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public double getCorpusIC() {
		return corpusIC;
	}

	public void setCorpusIC(double corpusIC) {
		this.corpusIC = corpusIC;
	}

	public double getIntrinsicIC() {
		return intrinsicIC;
	}

	public void setIntrinsicIC(double intrinsicIC) {
		this.intrinsicIC = intrinsicIC;
	}

}
