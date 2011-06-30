package ytex.kernel.model.corpus;

import java.io.Serializable;

public class ConceptLabelStatistic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int corpusConceptLabelStatisticId;
	CorpusLabelEvaluation corpusLabel;
	String conceptId;
	double mutualInfo;
	String jointDistribution;

	public int getCorpusConceptLabelStatisticId() {
		return corpusConceptLabelStatisticId;
	}

	public void setCorpusConceptLabelStatisticId(
			int corpusConceptLabelStatisticId) {
		this.corpusConceptLabelStatisticId = corpusConceptLabelStatisticId;
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

	public String getJointDistribution() {
		return jointDistribution;
	}

	public void setJointDistribution(String jointDistribution) {
		this.jointDistribution = jointDistribution;
	}

}
