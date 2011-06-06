package ytex.kernel.dao;

import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.FeatureEvaluation;

public interface ClassifierEvaluationDao {

	public abstract void saveClassifierEvaluation(ClassifierEvaluation eval,
			boolean saveInstanceEval);

	public abstract void saveFold(CrossValidationFold fold);

	public abstract void deleteCrossValidationFoldByName(String name);

	public abstract void saveFeatureEvaluation(
			FeatureEvaluation featureEvaluation);

	public abstract void deleteFeatureEvaluationByNameAndType(String name,
			String type);

	/**
	 * 
	 * @param eval
	 *            evaluation to save
	 * @param saveInstanceEval
	 *            save instance level evaluations - default false
	 * @param saveIRStats
	 *            save IR statistics - default true
	 * @param excludeTargetClassId
	 *            for semi-supervised learners, don't want to include the
	 *            unlabeled instances in computation of ir statistics. this
	 *            specifies the class id of the unlabeled instances (default 0)
	 */
	public void saveClassifierEvaluation(ClassifierEvaluation eval,
			boolean saveInstanceEval, boolean saveIRStats,
			Integer excludeTargetClassId);

}