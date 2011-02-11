package weka.classifiers.functions.supportVector;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;

/**
 * A friendlier version of the PrecomputedKernelMatrixKernel. Supports numeric
 * indexes. TODO: add support for additional attributes to simplify data file
 * management
 * 
 * @author vijay
 */
public class NumericPrecomputedKernelMatrixKernel extends
		PrecomputedKernelMatrixKernel {

	@Override
	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		Capabilities result = super.getCapabilities();
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		return result;
	}

}
