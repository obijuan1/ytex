package ytex.kernel.evaluator;

import ytex.kernel.tree.Node;

public class ConvolutionKernel implements Kernel {
	private Kernel delegateKernel;

	public Kernel getDelegateKernel() {
		return delegateKernel;
	}

	public void setDelegateKernel(Kernel delegateKernel) {
		this.delegateKernel = delegateKernel;
	}

	private double pow = 1;

	public double getPow() {
		return pow;
	}

	public void setPow(double pow) {
		this.pow = pow;
	}

	public double evaluate(Object c1, Object c2) {
		Node n1 = (Node) c1;
		Node n2 = (Node) c2;
		double d = 0;
		for (Node child1 : n1.getChildren()) {
			for (Node child2 : n2.getChildren()) {
				d += delegateKernel.evaluate(child1, child2);
			}
		}
		if (pow > 1)
			return Math.pow(d, pow);
		else
			return d;
	}

}
