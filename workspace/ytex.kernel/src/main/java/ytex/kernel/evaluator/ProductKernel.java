package ytex.kernel.evaluator;

import java.util.List;

public class ProductKernel implements Kernel {
	List<Kernel> delegateKernels;

	public List<Kernel> getDelegateKernels() {
		return delegateKernels;
	}

	public void setDelegateKernels(List<Kernel> delegateKernels) {
		this.delegateKernels = delegateKernels;
	}

	@Override
	public double evaluate(Object o1, Object o2) {
		double d = 1;
		for(Kernel k : delegateKernels) {
			d *= k.evaluate(o1, o2);
			if(d == 0)
				break;
		}
		return d;
	}

}
