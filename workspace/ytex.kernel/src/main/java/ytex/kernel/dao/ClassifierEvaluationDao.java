package ytex.kernel.dao;

import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.FeatureEvaluation;

public interface ClassifierEvaluationDao {

	public abstract void saveClassifierEvaluation(ClassifierEvaluation eval, boolean saveInstanceEval);

	public abstract void saveFold(CrossValidationFold fold);

	public abstract void deleteCrossValidationFoldByName(String name);

	public abstract void saveFeatureEvaluation(FeatureEvaluation featureEvaluation);

	public abstract void deleteFeatureEvaluationByNameAndType(String name,
			String type);

}