package ytex.kernel.model.corpus;

import java.io.Serializable;

public class CorpusEvaluation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String conceptGraphName;
	String conceptSetName;
	int corpusConceptEvaluationId;

	String corpusName;

	public CorpusEvaluation() {
		super();
	}

	public CorpusEvaluation(String corpusName, int corpusId) {
		super();
		this.corpusName = corpusName;
		this.corpusConceptEvaluationId = corpusId;
	}

	public String getConceptGraphName() {
		return conceptGraphName;
	}
	public String getConceptSetName() {
		return conceptSetName;
	}

	public int getCorpusConceptEvaluationId() {
		return corpusConceptEvaluationId;
	}

	public String getCorpusName() {
		return corpusName;
	}

	public void setConceptGraphName(String conceptGraphName) {
		this.conceptGraphName = conceptGraphName;
	}

	public void setConceptSetName(String conceptSetName) {
		this.conceptSetName = conceptSetName;
	}

	public void setCorpusConceptEvaluationId(int corpusConceptEvaluationId) {
		this.corpusConceptEvaluationId = corpusConceptEvaluationId;
	}

	public void setCorpusName(String corpusName) {
		this.corpusName = corpusName;
	}

}
