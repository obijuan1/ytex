package ytex.kernel.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ClassifierInstanceEvaluation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int predictedClassId;
	Integer targetClassId;
	ClassifierEvaluation classifierEvaluation;
	int classifierInstanceEvaluationId;
	Map<Integer, Double> classifierInstanceProbabilities = new HashMap<Integer, Double>();
	long instanceId;
	public int getPredictedClassId() {
		return predictedClassId;
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
	public long getInstanceId() {
		return instanceId;
	}
	public void setPredictedClassId(int classId) {
		this.predictedClassId = classId;
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
	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}
	public Integer getTargetClassId() {
		return targetClassId;
	}
	public void setTargetClassId(Integer targetClassId) {
		this.targetClassId = targetClassId;
	}
}
