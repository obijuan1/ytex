package ytex.kernel;

import java.io.IOException;
import java.util.Properties;

import ytex.kernel.model.ConceptGraph;

public interface IntrinsicInfoContentEvaluator {

	public static final String INTRINSIC_INFOCONTENT = "intrinsic-infocontent";
	public abstract void evaluateIntrinsicInfoContent(
			final Properties props) throws IOException;
	public abstract void evaluateIntrinsicInfoContent(String conceptGraphName,
			String conceptGraphDir, ConceptGraph cg) throws IOException;

}