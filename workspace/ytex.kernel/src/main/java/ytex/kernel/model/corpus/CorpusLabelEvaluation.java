package ytex.kernel.model.corpus;

import java.io.Serializable;

public class CorpusLabelEvaluation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int corpusConceptLabelEvaluationId;
	CorpusEvaluation corpus;
	String label;
	Integer foldId;
	
	public Integer getFoldId() {
		return foldId;
	}
	public void setFoldId(Integer foldId) {
		this.foldId = foldId;
	}
	public int getCorpusConceptLabelEvaluationId() {
		return corpusConceptLabelEvaluationId;
	}
	public void setCorpusConceptLabelEvaluationId(int corpusConceptLabelEvaluationId) {
		this.corpusConceptLabelEvaluationId = corpusConceptLabelEvaluationId;
	}
	public CorpusEvaluation getCorpus() {
		return corpus;
	}
	public void setCorpus(CorpusEvaluation corpus) {
		this.corpus = corpus;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
