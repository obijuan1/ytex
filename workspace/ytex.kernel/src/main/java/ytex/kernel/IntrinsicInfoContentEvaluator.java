package ytex.kernel;

import java.io.IOException;
import java.util.Properties;

public interface IntrinsicInfoContentEvaluator {

	public static final String INTRINSIC_INFOCONTENT = "intrinsic-infocontent";
	public abstract void evaluateIntrinsicInfoContent(
			final Properties props) throws IOException;

}