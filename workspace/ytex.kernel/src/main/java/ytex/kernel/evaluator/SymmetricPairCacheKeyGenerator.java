package ytex.kernel.evaluator;

import java.lang.reflect.Method;

import ytex.kernel.OrderedPair;

/**
 * cache key for a method that takes 2 arguments, and is symmetric - the order
 * of the arguments doesn't matter.
 * 
 * @author vijay
 * 
 */
public class SymmetricPairCacheKeyGenerator implements CacheKeyGenerator {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getCacheKey(Method method, Object[] args) {
		return new OrderedPair((Comparable) args[0], (Comparable) args[1]);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getCacheKey(Object o1, Object o2) {
		return new OrderedPair((Comparable) o1, (Comparable) o2);
	}

}
