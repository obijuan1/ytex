package ytex.kernel;

import ytex.kernel.model.SVMClassifierEvaluation;

public interface SVMParser {

	public abstract SVMClassifierEvaluation parseClassifierEvaluation(
			String name, String experiment, String label, String options,
			String predictionFile, String instanceFile, String modelFile,
			String instanceIdFile, String trainOutputFile,
			boolean storeProbabilities) throws Exception;

}