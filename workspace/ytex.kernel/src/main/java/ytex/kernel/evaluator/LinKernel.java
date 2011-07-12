package ytex.kernel.evaluator;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import ytex.kernel.ConceptSimilarityService;

public class LinKernel implements Kernel {
	private ConceptSimilarityService conceptSimilarityService;
	private CacheManager cacheManager;
	private Cache conceptSimCache;
	private String label = null;
	private double cutoff = 0;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getCutoff() {
		return cutoff;
	}

	public void setCutoff(double cutoff) {
		this.cutoff = cutoff;
	}

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public double evaluate(Object o1, Object o2) {
		double d = 0;
		String c1 = (String) o1;
		String c2 = (String) o2;
		if (c1 != null && c2 != null) {
			if (c1.equals(c2)) {
				d = 1;
			} else {
				// look in cache
//				String key = createKey(c1, c2);
//				Element e = conceptSimCache.get(key);
//				if (e != null) {
//					// it's there
//					d = (Double) e.getObjectValue();
//				} else {
					// it's not there - put it there
					d = conceptSimilarityService.filteredLin(c1, c2, label,
							cutoff);
//					conceptSimCache.put(new Element(key, d));
//				}
			}
		}
		return d;
	}

	private String createKey(String c1, String c2) {
		if (c1.compareTo(c2) < 0) {
			return new StringBuilder(c1).append("-").append(c2).toString();
		} else {
			return new StringBuilder(c2).append("-").append(c1).toString();
		}
	}

	public void init() {
		conceptSimCache = cacheManager.getCache("linSimCache");
	}
}
