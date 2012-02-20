package ytex.kernel.evaluator;

import org.springframework.beans.factory.InitializingBean;

/*
 * @deprecated
 */
public class LinKernel extends SemanticSimKernel implements InitializingBean {
	public LinKernel() {
		super();
		this.setMetricNames("LIN");
	}

}
