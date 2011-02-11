package ytex.kernel.model;

import java.io.Serializable;

public class Corpus implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String corpusName;
	int corpusId;

	public String getCorpusName() {
		return corpusName;
	}

	public void setCorpusName(String corpusName) {
		this.corpusName = corpusName;
	}

	public int getCorpusId() {
		return corpusId;
	}

	public void setCorpusId(int corpusId) {
		this.corpusId = corpusId;
	}

	public Corpus(String corpusName, int corpusId) {
		super();
		this.corpusName = corpusName;
		this.corpusId = corpusId;
	}

	public Corpus() {
		super();
	}

}
