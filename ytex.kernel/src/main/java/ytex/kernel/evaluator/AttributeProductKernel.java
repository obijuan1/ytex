package ytex.kernel.evaluator;


/**
 * Expects numeric values as input. Returns the product of the specified values,
 * 
 * @author vijay
 * 
 */
public class AttributeProductKernel implements Kernel {

	@Override
	public double evaluate(Object o1, Object o2) {
		double d = 0;
		Number num1 = (Number) o1;
		Number num2 = (Number) o2;
		if (num1 != null && num2 != null) {
			d = num1.doubleValue() * num2.doubleValue();
		}
		return d;
	}

}
