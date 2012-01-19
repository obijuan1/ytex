package ytex.kernel.metric;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	String corpusLcs;
	Double corpusLcsIC;
	String intrinsicLcs;

	Double intrinsicLcsIC;

	Integer lcsDist;

	Set<String> lcses;

	Map<String, List<List<String>>> lcsPathMap;

	public SimilarityInfo() {
		super();
	}

	public String getCorpusLcs() {
		return corpusLcs;
	}

	public Double getCorpusLcsIC() {
		return corpusLcsIC;
	}

	public String getIntrinsicLcs() {
		return intrinsicLcs;
	}

	public Double getIntrinsicLcsIC() {
		return intrinsicLcsIC;
	}

	public Integer getLcsDist() {
		return lcsDist;
	}

	public Set<String> getLcses() {
		return lcses;
	}

	public Map<String, List<List<String>>> getLcsPathMap() {
		return lcsPathMap;
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

	public void setLcses(Set<String> lcses) {
		this.lcses = lcses;
	}

	public void setLcsPathMap(Map<String, List<List<String>>> lcsPathMap) {
		this.lcsPathMap = lcsPathMap;
	}

	@Override
	public String toString() {
		return "SimilarityInfo [lcses=" + lcses + ", lcsPathMap=" + lcsPathMap
				+ "]";
	}
}
