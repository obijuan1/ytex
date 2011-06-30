package ytex.kernel.model.corpus;

import java.io.Serializable;

public class ConceptInformationContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String conceptId;
	private CorpusEvaluation corpus;
	private int corpusConceptInformationContentId;
	private double frequency;

	private Double informationContent;

	public ConceptInformationContent() {
		super();
	}
	public ConceptInformationContent(CorpusEvaluation corpus, String conceptId, double frequency,
			Double informationContent) {
		super();
		this.corpus = corpus;
		this.conceptId = conceptId;
		this.frequency = frequency;
		this.informationContent = informationContent;
	}

	public String getConceptId() {
		return conceptId;
	}

	public CorpusEvaluation getCorpus() {
		return corpus;
	}

	public int getCorpusConceptInformationContentId() {
		return corpusConceptInformationContentId;
	}
	public double getFrequency() {
		return frequency;
	}


	public Double getInformationContent() {
		return informationContent;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public void setCorpus(CorpusEvaluation corpus) {
		this.corpus = corpus;
	}

	public void setCorpusConceptInformationContentId(
			int corpusConceptInformationContentId) {
		this.corpusConceptInformationContentId = corpusConceptInformationContentId;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public void setInformationContent(Double informationContent) {
		this.informationContent = informationContent;
	}

}
