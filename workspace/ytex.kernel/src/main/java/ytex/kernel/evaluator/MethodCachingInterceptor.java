package ytex.kernel.evaluator;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * simple caching interceptor. we require a cacheName and cacheKeyGenerator.
 * we don't use AOP style configuration because we reuse the same classes (kernels)
 * in very different contexts.  sometimes we want to cache, sometimes we don't.
 * therefore, use old-school ProxyFactoryBean with this interceptor.
 * 
 * @author vijay
 * 
 */
public class MethodCachingInterceptor implements MethodInterceptor,
		InitializingBean {
	private static final Log log = LogFactory
			.getLog(MethodCachingInterceptor.class);

	private CacheManager cacheManager;
	private String cacheName;
	private Cache cache;
	private CacheKeyGenerator cacheKeyGenerator;
	private String methodName;

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

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

	public Object invoke(final MethodInvocation methodInvocation)
			throws Throwable {
		Object methodReturn = null;
		if (methodName == null
				|| methodName.equals(methodInvocation.getMethod().getName())) {
			final Object cacheKey = this.cacheKeyGenerator.getCacheKey(
					methodInvocation.getMethod(),
					methodInvocation.getArguments());
			final Element cacheElement = cache.get(cacheKey);
			if (cacheElement == null) {
				methodReturn = methodInvocation.proceed();
				cache.put(new Element(cacheKey, (Serializable) methodReturn));
			} else {
				methodReturn = cacheElement.getValue();
			}
		} else {
			methodReturn = methodInvocation.proceed();
		}

		return methodReturn;
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
		cache = cacheManager.getCache(cacheName);
	}
}
