package ytex.kernel.dao;

import java.util.List;

import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.FeatureInfogain;

public interface ClassifierEvaluationDao {

	public abstract void saveClassifierEvaluation(ClassifierEvaluation eval);

	public abstract void saveFold(CrossValidationFold fold);

	public abstract void deleteCrossValidationFoldByName(String name);

	public abstract void saveInfogain(List<FeatureInfogain> foldInfogainList);

}