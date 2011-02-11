package ytex.kernel.model;

import java.io.Serializable;

public class InfoContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int infoContentId;
	private Corpus corpus;

	private String conceptId;
	private double frequency;
	private double informationContent;

	public int getInfoContentId() {
		return infoContentId;
	}

	public void setInfoContentId(int infoContentId) {
		this.infoContentId = infoContentId;
	}

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

	public double getInformationContent() {
		return informationContent;
	}

	public void setInformationContent(double informationContent) {
		this.informationContent = informationContent;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
}
