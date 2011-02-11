package ytex.kernel.evaluator;

import ytex.kernel.tree.Node;

/**
 * Evaluate negation status and certainty of named entities. If negation status differs,
 * multiply convolution on concepts by -1.
 * If certainty differs, multiply by 0.5.  
 * This assumes that possible values for certainty are certain/uncertain.
 */
public class NamedEntityNegationKernel extends ConvolutionKernel {
	private static final String CONF_ATTR = "confidence";
	private static final String CERT_ATTR = "certainty";

	@Override
	public double evaluate(Object c1, Object c2) {
		Node ne1 = (Node) c1;
		Node ne2 = (Node) c2;
		Number confidence1 = (Number) ne1.getValue().get(CONF_ATTR);
		Number confidence2 = (Number) ne2.getValue().get(CONF_ATTR);
		Integer certainty1 = (Integer) ne1.getValue().get(CERT_ATTR);
		Integer certainty2 = (Integer) ne2.getValue().get(CERT_ATTR);
		double negationFactor = 1;
		if (confidence1 != null && confidence2 != null
				&& !confidence1.equals(confidence2))
			negationFactor = -1;
		double certaintyFactor = 1;
		if (certainty1 != null && certainty1 != null
				&& !certainty1.equals(certainty2))
			certaintyFactor = 0.5;
		return negationFactor * certaintyFactor * super.evaluate(c1, c2);
	}

}
