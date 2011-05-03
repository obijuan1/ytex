package ytex.kernel.evaluator;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import ytex.kernel.tree.InstanceTreeBuilder;
import ytex.kernel.tree.Node;

public class TreePrinter {

	public static void main(String args[]) throws IOException, ClassNotFoundException {
		String beanRefContext = "classpath*:kernelBeanRefContext.xml";
		String contextName = "kernelApplicationContext";
		ApplicationContext appCtx = (ApplicationContext) ContextSingletonBeanFactoryLocator
				.getInstance(beanRefContext)
				.useBeanFactory(contextName).getFactory();
		ApplicationContext appCtxSource = appCtx;
		InstanceTreeBuilder builder = appCtxSource.getBean(
				"instanceTreeBuilder", InstanceTreeBuilder.class);
		Map<Integer, Node> instanceMap = builder.loadInstanceTrees(args[0]);
		for(Node node : instanceMap.values())
			printTree(node, 0);
	}

	private static void printTree(Node node, int depth) {
		for(int i = 0; i<= depth; i++) {
			System.out.print("  ");
		}
		System.out.println(node);
		for(Node child : node.getChildren()) {
			printTree(child, depth+1);
		}
	}
}
