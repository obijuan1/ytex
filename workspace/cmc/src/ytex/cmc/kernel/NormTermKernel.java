package ytex.cmc.kernel;

import ytex.kernel.evaluator.ConvolutionKernel;
import ytex.kernel.tree.Node;

public class NormTermKernel extends ConvolutionKernel {
	@Override
	public double evaluate(Object o1, Object o2) {
		Node nt1 = (Node)o1;
		Node nt2 = (Node)o2;
		if(nt1.getChildren().isEmpty() || nt2.getChildren().isEmpty()) {
			return nt1.getValue().get("normTerm").equals(nt2.getValue().get("normTerm")) ? 1 : 0;
		} else {
			return super.evaluate(nt1, nt2);
		}
	}

}
