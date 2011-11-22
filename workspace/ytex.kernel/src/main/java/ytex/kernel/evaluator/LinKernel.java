package ytex.kernel.evaluator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import ytex.kernel.ConceptSimilarityService;

public class LinKernel extends CacheKernel implements InitializingBean {
	private static final Log log = LogFactory.getLog(LinKernel.class);
	private Map<String, Double> conceptFilter = null;
	private ConceptSimilarityService conceptSimilarityService;
	private double cutoff = 0;
	private String label = null;
	private Integer rankCutoff = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.initializeConceptFilter();
	}

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public double getCutoff() {
		return cutoff;
	}

	public String getLabel() {
		return label;
	}

	public Integer getRankCutoff() {
		return rankCutoff;
	}

	protected void initializeConceptFilter() {
		if (rankCutoff != null) {
			conceptFilter = new HashMap<String, Double>();
			cutoff = conceptSimilarityService.loadConceptFilter(label,
					rankCutoff, conceptFilter);
			if (conceptFilter.isEmpty()) {
				log.warn("no concepts that matched the threshold for supervised semantic similarity. label="
						+ label + ", rankCutoff=" + rankCutoff);
			}
		}
	}

	/**
	 * override CacheKernel - don't bother caching evaluation if the concepts
	 * are not in the conceptFilter.
	 */
	@Override
	public double evaluate(Object o1, Object o2) {
		if (this.conceptFilter != null
				&& !(conceptFilter.containsKey((String) o1) || conceptFilter
						.containsKey((String) o2))) {
			return 0d;
		} else {
			return super.evaluate(o1, o2);
		}
	}

	@Override
	public double innerEvaluate(Object o1, Object o2) {
		double d = 0;
		String c1 = (String) o1;
		String c2 = (String) o2;
		if (c1 != null && c2 != null) {
			if (c1.equals(c2)) {
				d = 1;
			} else {
				d = conceptSimilarityService.filteredLin(c1, c2, conceptFilter);
			}
		}
		return d;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	public void setCutoff(double cutoff) {
		this.cutoff = cutoff;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setRankCutoff(Integer rankCutoff) {
		this.rankCutoff = rankCutoff;
	}

}
