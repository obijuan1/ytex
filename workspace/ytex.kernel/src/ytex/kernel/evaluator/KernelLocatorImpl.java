package ytex.kernel.evaluator;

import java.util.Map;

public class KernelLocatorImpl {
	private Map<String,Kernel> objectTypeToKernelMap;
	
	public Kernel getKernel(Object o) {
		if(o instanceof Map<?,?>) {
			Object oClassName = ((Map<?,?>)o).get("class");
			return objectTypeToKernelMap.get(oClassName);
		} else {
			return objectTypeToKernelMap.get(o.getClass().getName());
		}
	}

}
