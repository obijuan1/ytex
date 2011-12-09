package ytex.kernel;

import java.io.IOException;

import ytex.kernel.ImputedFeatureEvaluatorImpl.Parameters;

public interface ImputedFeatureEvaluator {
	public enum MeasureType {
		MUTUALINFO("mutualinfo"), INFOGAIN("infogain");
		String name;

		public String getName() {
			return name;
		}

		MeasureType(String name) {
			this.name = name;
		}
	};

	public static final String SUFFIX_PROP = "-propagated";
	public static final String SUFFIX_IMPUTED = "-imputed";
	public static final String SUFFIX_IMPUTED_FILTERED = "-imputed-filt";

	public abstract boolean evaluateCorpus(String propFile) throws IOException;

	boolean evaluateCorpus(Parameters params);

}