package ytex.ws;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import ytex.kernel.ConceptPair;
import ytex.kernel.metric.SimilarityInfo;

@XmlRootElement(name = "conceptPairSimilarity")
public class ConceptPairSimilarity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ConceptPair conceptPair;
	List<Double> similarities;
	SimilarityInfo similarityInfo;
	
	public ConceptPairSimilarity() {
		super();
	}
	public ConceptPair getConceptPair() {
		return conceptPair;
	}
	public List<Double> getSimilarities() {
		return similarities;
	}
	public SimilarityInfo getSimilarityInfo() {
		return similarityInfo;
	}

	public void setConceptPair(ConceptPair conceptPair) {
		this.conceptPair = conceptPair;
	}

	public void setSimilarities(List<Double> similarities) {
		this.similarities = similarities;
	}

	public void setSimilarityInfo(SimilarityInfo similarityInfo) {
		this.similarityInfo = similarityInfo;
	}
}
