package ytex.kernel.evaluator;

import java.lang.reflect.Method;

public interface CacheKeyGenerator {
	public Object getCacheKey(Method method, Object[] args);
	public Object getCacheKey(Object o1, Object o2);
}
