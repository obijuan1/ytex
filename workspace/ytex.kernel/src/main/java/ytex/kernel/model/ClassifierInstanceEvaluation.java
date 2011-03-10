package ytex.kernel.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ClassifierInstanceEvaluation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int classId;
	ClassifierEvaluation classifierEvaluation;
	int classifierInstanceEvaluationId;
	Map<Integer, Double> classifierInstanceProbabilities = new HashMap<Integer, Double>();
	int instanceId;
	public int getClassId() {
		return classId;
	}
	public ClassifierEvaluation getClassifierEvaluation() {
		return classifierEvaluation;
	}
	public int getClassifierInstanceEvaluationId() {
		return classifierInstanceEvaluationId;
	}
	public Map<Integer, Double> getClassifierInstanceProbabilities() {
		return classifierInstanceProbabilities;
	}
	public int getInstanceId() {
		return instanceId;
	}
	public void setClassId(int classId) {
		this.classId = classId;
	}
	public void setClassifierEvaluation(ClassifierEvaluation classifierEvaluation) {
		this.classifierEvaluation = classifierEvaluation;
	}
	public void setClassifierInstanceEvaluationId(int classifierInstanceEvaluationId) {
		this.classifierInstanceEvaluationId = classifierInstanceEvaluationId;
	}
	public void setClassifierInstanceProbabilities(
			Map<Integer, Double> classifierInstanceProbabilities) {
		this.classifierInstanceProbabilities = classifierInstanceProbabilities;
	}
	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}
}
