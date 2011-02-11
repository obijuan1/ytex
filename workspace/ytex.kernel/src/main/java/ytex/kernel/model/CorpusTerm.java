package ytex.kernel.model;

import java.io.Serializable;

public class CorpusTerm implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int corpusTermId;
	private Corpus corpus;
	private String conceptId;
	private double frequency;
	/**
	 * todo ic is concept-graph-specific
	 */
	private Double informationContent;

	public Corpus getCorpus() {
		return corpus;
	}

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public CorpusTerm(Corpus corpus, String conceptId, double frequency,
			Double informationContent) {
		super();
		this.corpus = corpus;
		this.conceptId = conceptId;
		this.frequency = frequency;
		this.informationContent = informationContent;
	}

	public CorpusTerm() {
		super();
	}

	public void setCorpusTermId(int corpusTermId) {
		this.corpusTermId = corpusTermId;
	}

	public int getCorpusTermId() {
		return corpusTermId;
	}

	public void setInformationContent(Double informationContent) {
		this.informationContent = informationContent;
	}

	public Double getInformationContent() {
		return informationContent;
	}

}
