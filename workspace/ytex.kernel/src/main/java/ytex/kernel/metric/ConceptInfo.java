package ytex.kernel.metric;

/**
 * we run into out of memory errors when preloading the intrinsic ic for large
 * concept graphs. 'compress' the intrinsic ic by using short/float instead of
 * int/double.
 * 
 * @author vijay
 * 
 */
public class ConceptInfo {
	private String conceptId;
	private short depth;
//	private float corpusIC;
//	private float intrinsicIC;
	private double corpusIC;
	private double intrinsicIC;

	public ConceptInfo() {
		super();
	}

	public ConceptInfo(String conceptId, int depth, double corpusIC,
			double intrinsicIC) {
		super();
		this.conceptId = conceptId;
		this.depth = (short) depth;
//		this.corpusIC = (float) corpusIC;
//		this.intrinsicIC = (float) intrinsicIC;
		this.corpusIC = corpusIC;
		this.intrinsicIC = intrinsicIC;
	}

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public int getDepth() {
		return (int) depth;
	}

	public void setDepth(int depth) {
		this.depth = (short) depth;
	}

	public double getCorpusIC() {
		return (double) corpusIC;
	}

	public void setCorpusIC(double corpusIC) {
//		this.corpusIC = (float) corpusIC;
		this.corpusIC = (double) corpusIC;
	}

	public double getIntrinsicIC() {
		return (double) intrinsicIC;
	}

	public void setIntrinsicIC(double intrinsicIC) {
//		this.intrinsicIC = (float) intrinsicIC;
		this.intrinsicIC = (double) intrinsicIC;
	}

}
