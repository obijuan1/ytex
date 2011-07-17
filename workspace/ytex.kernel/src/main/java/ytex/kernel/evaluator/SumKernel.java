package ytex.kernel.evaluator;

import java.util.List;

/**
 * apply all the delegate kernels to the objects, sum them up
 */
public class SumKernel extends CacheKernel {
	List<Kernel> delegateKernels;

	public List<Kernel> getDelegateKernels() {
		return delegateKernels;
	}

	public void setDelegateKernels(List<Kernel> delegateKernels) {
		this.delegateKernels = delegateKernels;
	}

	/**
	 * 
	 */
	@Override
	public double innerEvaluate(Object o1, Object o2) {
		double d = 0;
		for(Kernel k : delegateKernels) {
			d += k.evaluate(o1, o2);
		}
		return d;
	}
}
