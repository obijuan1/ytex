package ytex.kernel.evaluator;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import ytex.kernel.ConceptSimilarityService;

public class ConceptKernel implements Kernel {
	private ConceptSimilarityService conceptSimilarityService;
	private CacheManager cacheManager;
	private Cache conceptSimCache;

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
				// Set<String> tuis1 = cuiTuiMap.get(c1);
				// Set<String> tuis2 = cuiTuiMap.get(c2);
				// Set<Integer> tuis1 = cuiMainSuiMap.get(c1);
				// Set<Integer> tuis2 = cuiMainSuiMap.get(c2);
				// // only compare the two if they have a common semantic type
				// if (tuis1 != null && tuis2 != null
				// && !Collections.disjoint(tuis1, tuis2)) {
				// look in cache
				String key = createKey(c1, c2);
				Element e = conceptSimCache.get(key);
				if (e != null) {
					// it's there
					d = (Double) e.getObjectValue();
				} else {
					// it's not there - put it there
					d = conceptSimilarityService.lch(c1, c2)
							* conceptSimilarityService.lin("i2b2.2008", c1, c2);
					conceptSimCache.put(new Element(key, d));
				}
				// }
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
		conceptSimCache = cacheManager.getCache("conceptSimCache");
	}	
}