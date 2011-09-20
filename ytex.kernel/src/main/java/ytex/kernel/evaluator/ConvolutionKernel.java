package ytex.kernel.evaluator;

import ytex.kernel.tree.Node;

/**
 * Apply the delegate kernel to the children of the given nodes.
 * 
 * @author vijay
 * 
 */
public class ConvolutionKernel implements Kernel {
	private Kernel delegateKernel;
	private String nodeType;

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

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

	/**
	 * c1 and c2 must be Nodes. if the nodeType field is set, the type of the
	 * Nodes must match nodeType field for us to evaluate the delegateKernel on
	 * them.
	 * 
	 * @return sum( sum( K(child(i),child(j) ) ) )
	 */
	public double evaluate(Object c1, Object c2) {
		Node n1 = (Node) c1;
		Node n2 = (Node) c2;
		double d = 0;
		for (Node child1 : n1.getChildren()) {
			for (Node child2 : n2.getChildren()) {
				// if node type specified, they have to match
				if (getNodeType() == null
						|| (getNodeType().equals(child1.getType()) && getNodeType()
								.equals(child2.getType()))) {
					d += delegateKernel.evaluate(child1, child2);
				}
			}
		}
		if (pow > 1)
			return Math.pow(d, pow);
		else
			return d;
	}

}
