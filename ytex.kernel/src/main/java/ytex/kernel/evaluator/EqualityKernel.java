package ytex.kernel.evaluator;

public class EqualityKernel implements Kernel {

	@Override
	public double evaluate(Object o1, Object o2) {
		if(o1 == null && o2 == null)
			return 1;
		else if(o1 == null && o2 != null)
			return 0;
		else if(o2 == null && o1 != null)
			return 0;
		else 
			return o1.equals(o2) ? 1 : 0;
	}

}
