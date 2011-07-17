package ytex.kernel.evaluator;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.InitializingBean;

/**
 * consolidate caching in this class. using AOP is very slow!!! According to the
 * profiler, Class.isInterface() (called by AOP) eats up a large chunk of CPU.
 * Caching is enabled if the cacheName is specified.
 * <p>
 * By default, we assume that the objects upon which we evaluate the kernel
 * support the Comparable interface. If not, set the cacheKeyGenerator to a
 * different class (default is SymmetricPairCacheKeyGenerator).
 * 
 * @author vijay
 * 
 */
public abstract class CacheKernel implements Kernel, InitializingBean {

	private CacheManager cacheManager;
	private String cacheName;
	private Cache cache;
	private CacheKeyGenerator cacheKeyGenerator = new SymmetricPairCacheKeyGenerator();

	public CacheKeyGenerator getCacheKeyGenerator() {
		return cacheKeyGenerator;
	}

	public void setCacheKeyGenerator(CacheKeyGenerator cacheKeyGenerator) {
		this.cacheKeyGenerator = cacheKeyGenerator;
	}

	/**
	 * @return the cacheManager
	 */
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public String getCacheName() {
		return cacheName;
	}

	public abstract double innerEvaluate(Object o1, Object o2);

	final public double evaluate(Object o1, Object o2) {
		double dEval = 0d;
		if (o1 == null || o2 == null) {
			dEval = 0;
		} else if (o1.equals(o2)) {
			dEval = 1;
		} else {
			Element e = null;
			Object cacheKey = null;
			if (cache != null) {
				cacheKey = cacheKeyGenerator.getCacheKey(o1, o2);
				e = this.cache.get(cacheKey);
				if (e != null) {
					dEval = (Double) e.getValue();
				}
			}
			if (cache == null || e == null) {
				dEval = innerEvaluate(o1, o2);
				if (cache != null) {
					cache.put(new Element(cacheKey, dEval));
				}
			}
		}
		return dEval;
	}

	/**
	 * @param cacheManager
	 *            the cacheManager to set
	 */
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (cacheName != null) {
			cache = cacheManager.getCache(cacheName);
		}
	}
}
