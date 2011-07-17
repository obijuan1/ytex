package ytex.kernel.evaluator;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Return norm of delegate kernel: <code>k(x,y)/sqrt(k(x,x)*k(y,y)</code>. Cache
 * the result if cacheNorm = true (default). If the delegate kernel is fast
 * (e.g. it's using caching itself / trivial operation) caching the norm will
 * slow things down.
 * 
 * @author vijay
 * 
 */
public class NormKernel implements Kernel {
	private static final Log log = LogFactory.getLog(NormKernel.class);

	private Cache normCache;
	private CacheManager cacheManager;
	private Kernel delegateKernel;
	private boolean cacheNorm = true;

	public boolean isCacheNorm() {
		return cacheNorm;
	}

	public void setCacheNorm(boolean cacheNorm) {
		this.cacheNorm = cacheNorm;
	}

	public NormKernel(Kernel delegateKernel) {
		this.delegateKernel = delegateKernel;
	}

	public NormKernel() {
		super();
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public Kernel getDelegateKernel() {
		return delegateKernel;
	}

	public void setDelegateKernel(Kernel delegateKernel) {
		this.delegateKernel = delegateKernel;
	}

	public double getNorm(Object o1) {
		double norm = 0;
		if (o1 != null) {
			Element cachedNorm = null;
			if (this.isCacheNorm())
				cachedNorm = normCache.get(o1);
			if (cachedNorm == null) {
				norm = Math.sqrt(delegateKernel.evaluate(o1, o1));
				if (this.isCacheNorm())
					normCache.put(new Element(o1, norm));
			} else {
				norm = (Double) cachedNorm.getObjectValue();
			}
		}
		return norm;
	}

	public double evaluate(Object o1, Object o2) {
		double d = 0;
		if (o1 == null || o2 == null) {
			d = 0;
		} else {
			double norm1 = getNorm(o1);
			double norm2 = getNorm(o2);
			if (norm1 != 0 && norm2 != 0)
				d = delegateKernel.evaluate(o1, o2) / (norm1 * norm2);
		}
		if (log.isTraceEnabled()) {
			log.trace("K<" + o1 + "," + o2 + "> = " + d);
		}
		return d;
	}

	public void init() {
		normCache = cacheManager.getCache("normCache");
	}
}