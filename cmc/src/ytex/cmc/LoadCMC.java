package ytex.cmc;

import java.io.File;
import java.io.InputStream;

import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

public class LoadCMC {
	static ApplicationContext cmcApplicationContext = null;
	static {
		String beanRefContext = "classpath*:cmcBeanRefContext.xml";
		cmcApplicationContext = (ApplicationContext) ContextSingletonBeanFactoryLocator
				.getInstance(beanRefContext).useBeanFactory(
						"cmcApplicationContext").getFactory();
	}

	public static ApplicationContext getApplicationContext() {
		return cmcApplicationContext;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DocumentLoader l = (DocumentLoader)cmcApplicationContext.getBean("documentLoader");
		l.process(args[0]+ File.separator + "training/2007ChallengeTrainData.xml", "train");
		l.process(args[0]+ File.separator + "testing-with-codes/2007ChallengeTestDataCodes.xml", "test");
	}

}
