package ytex.kernel.evaluator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * compute the product of delegate kernels
 * 
 * @author vijay
 * 
 */
public class ProductKernel extends CacheKernel {
	private static final Log log = LogFactory.getLog(ProductKernel.class);
	List<Kernel> delegateKernels;

	public List<Kernel> getDelegateKernels() {
		return delegateKernels;
	}

	public void setDelegateKernels(List<Kernel> delegateKernels) {
		this.delegateKernels = delegateKernels;
	}

	@Override
	public double innerEvaluate(Object o1, Object o2) {
		double d = 1;
		for (Kernel k : delegateKernels) {
			d *= k.evaluate(o1, o2);
			if (d == 0)
				break;
		}
		if (log.isTraceEnabled()) {
			log.trace(new StringBuilder("K<").append(o1)
					.append(",").append(o2).append("> = ").append(d));
		}
		return d;
	}
}
