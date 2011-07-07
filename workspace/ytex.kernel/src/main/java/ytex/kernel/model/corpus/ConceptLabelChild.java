package ytex.kernel.model.corpus;

import java.io.Serializable;

public class ConceptLabelChild implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int corpusConceptLabelChildId;
	CorpusLabelEvaluation corpusLabel;
	String conceptId;
	double mutualInfo;

	public int getCorpusConceptLabelChildId() {
		return corpusConceptLabelChildId;
	}

	public void setCorpusConceptLabelChildId(int corpusConceptLabelChildId) {
		this.corpusConceptLabelChildId = corpusConceptLabelChildId;
	}

	public CorpusLabelEvaluation getCorpusLabel() {
		return corpusLabel;
	}

	public void setCorpusLabel(CorpusLabelEvaluation corpusLabel) {
		this.corpusLabel = corpusLabel;
	}

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public double getMutualInfo() {
		return mutualInfo;
	}

	public void setMutualInfo(double mutualInfo) {
		this.mutualInfo = mutualInfo;
	}

}
