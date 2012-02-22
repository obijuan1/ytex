package ytex.kernel.metric;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Data structure to hold information on the lcs's, paths, and final selected
 * LCS for a similarity measure. This is used by all the SimilarityMetrics
 * called for a pair of concepts - we load the lcs info only once for each
 * concept pair we are comparing.
 * <p/>
 * lcses - set of lcses.
 * <p/>
 * lcsDist - distance between concepts through lcs
 * <p/>
 * lcsPathMap - map of lcs to paths through the lcs between concept pairs. If
 * this is non-null, then we fill this in. else we ignore this.
 * <p/>
 * corpusLcs, corpusLcsIC - the lcs selected for computing the similarity
 * (relevant only to Information Content based measures)
 * <p/>
 * intrinsincLcs, intrinsicLcsIC - the lcs selected for computing the similarity
 * (relevant only to Intrinsic Information Content based measures)
 * 
 * @author vijay
 * 
 */
public class SimilarityInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String corpusLcs;

	private Double corpusLcsIC;

	private String intrinsicLcs;

	private Double intrinsicLcsIC;

	private Integer lcsDist;
	
	private List<LCSPath> lcsPaths;

	@XmlTransient
	private Set<String> lcses = new HashSet<String>(1);

	public Set<String> getLcses() {
		return lcses;
	}

	public SimilarityInfo() {
		super();
	}

	@XmlAttribute 
	public String getCorpusLcs() {
		return corpusLcs;
	}

	@XmlAttribute 
	public Double getCorpusLcsIC() {
		return corpusLcsIC;
	}

	@XmlAttribute 
	public String getIntrinsicLcs() {
		return intrinsicLcs;
	}

	@XmlAttribute 
	public Double getIntrinsicLcsIC() {
		return intrinsicLcsIC;
	}

	@XmlAttribute 
	public Integer getLcsDist() {
		return lcsDist;
	}

	@XmlElement
	public List<LCSPath> getLcsPaths() {
		return lcsPaths;
	}

	public void setCorpusLcs(String corpusLcs) {
		this.corpusLcs = corpusLcs;
	}

	public void setCorpusLcsIC(Double corpusLcsIC) {
		this.corpusLcsIC = corpusLcsIC;
	}

	public void setIntrinsicLcs(String intrinsicLcs) {
		this.intrinsicLcs = intrinsicLcs;
	}

	public void setIntrinsicLcsIC(Double intrinsicLcsIC) {
		this.intrinsicLcsIC = intrinsicLcsIC;
	}

	public void setLcsDist(Integer lcsDist) {
		this.lcsDist = lcsDist;
	}

	public void setLcsPaths(List<LCSPath> lcsPaths) {
		this.lcsPaths = lcsPaths;
	}
}
