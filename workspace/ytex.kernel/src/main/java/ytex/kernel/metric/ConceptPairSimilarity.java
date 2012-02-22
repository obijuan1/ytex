package ytex.kernel.metric;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "conceptPairSimilarity")
public class ConceptPairSimilarity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConceptPair conceptPair;

	private List<Double> similarities;
	private SimilarityInfo similarityInfo;

	public ConceptPairSimilarity() {
		super();
	}

	@XmlElement
	public ConceptPair getConceptPair() {
		return conceptPair;
	}

	@XmlAttribute
	public List<Double> getSimilarities() {
		return similarities;
	}

	@XmlElement
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
