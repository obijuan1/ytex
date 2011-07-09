package ytex.kernel;

import java.io.IOException;

import ytex.kernel.CorpusLabelEvaluatorImpl.Parameters;

public interface CorpusLabelEvaluator {

	public static final String MUTUALINFO_CHILD = "mutualinfo-child";
	public static final String MUTUALINFO_PARENT = "mutualinfo-parent";
	public static final String MUTUALINFO = "mutualinfo";

	public abstract boolean evaluateCorpus(String propFile) throws IOException;

	boolean evaluateCorpus(Parameters params);

}