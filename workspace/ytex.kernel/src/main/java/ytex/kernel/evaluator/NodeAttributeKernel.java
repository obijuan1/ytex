package ytex.kernel.evaluator;

import ytex.kernel.tree.Node;

/**
 * Extract a node attribute and run the delegate kernel on the attribute.
 * 
 * @author vijay
 * 
 */
public class NodeAttributeKernel implements Kernel {

	private Kernel delegateKernel;
	private String attributeName;

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public Kernel getDelegateKernel() {
		return delegateKernel;
	}

	public void setDelegateKernel(Kernel delegateKernel) {
		this.delegateKernel = delegateKernel;
	}

	@Override
	public double evaluate(Object o1, Object o2) {
		Node n1 = (Node) o1;
		Node n2 = (Node) o2;
		if (n1 != null && n2 != null && n1.getType().equals(n2.getType())) {
			Object attr1 = n1.getValue().get(attributeName);
			Object attr2 = n2.getValue().get(attributeName);
			if (n1 != null && n2 != null) {
				return delegateKernel.evaluate(attr1, attr2);
			}
		}
		return 0;
	}
}
