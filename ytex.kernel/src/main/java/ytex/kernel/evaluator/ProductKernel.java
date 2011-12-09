package ytex.kernel.evaluator;

import java.util.Arrays;
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
	/**
	 * use array instead of list. when running thread dumps, see a lot of action
	 * in list.size(). may be a fluke, but can't hurt
	 */
	Kernel[] delegateKernels;

	public List<Kernel> getDelegateKernels() {
		return Arrays.asList(delegateKernels);
	}

	public void setDelegateKernels(List<Kernel> delegateKernels) {
		this.delegateKernels = new Kernel[delegateKernels.size()];
		for (int i = 0; i < this.delegateKernels.length; i++)
			this.delegateKernels[i] = delegateKernels.get(i);
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
			log.trace(new StringBuilder("K<").append(o1).append(",").append(o2)
					.append("> = ").append(d));
		}
		return d;
	}
}
