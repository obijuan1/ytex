package ytex.kernel;

import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

public class KernelContextHolder {
	static ApplicationContext kernelApplicationContext = null;
	static {
		String beanRefContext = "classpath*:kernelBeanRefContext.xml";
		kernelApplicationContext = (ApplicationContext) ContextSingletonBeanFactoryLocator
				.getInstance(beanRefContext).useBeanFactory(
						"kernelApplicationContext").getFactory();
	}

	public static ApplicationContext getApplicationContext() {
		return kernelApplicationContext;
	}

}
