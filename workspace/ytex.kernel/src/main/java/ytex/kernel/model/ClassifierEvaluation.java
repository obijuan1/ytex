package ytex.kernel.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ClassifierEvaluation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int classifierEvaluationId;
	String name;
	String fold;
	String algorithm;
	String label;
	String options;
	Map<Integer, ClassifierInstanceEvaluation> classifierInstanceEvaluations = new HashMap<Integer, ClassifierInstanceEvaluation>();
	public int getClassifierEvaluationId() {
		return classifierEvaluationId;
	}
	public void setClassifierEvaluationId(int classifierEvaluationId) {
		this.classifierEvaluationId = classifierEvaluationId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFold() {
		return fold;
	}
	public void setFold(String fold) {
		this.fold = fold;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getOptions() {
		return options;
	}
	public void setOptions(String options) {
		this.options = options;
	}
	public Map<Integer, ClassifierInstanceEvaluation> getClassifierInstanceEvaluations() {
		return classifierInstanceEvaluations;
	}
	public void setClassifierInstanceEvaluations(
			Map<Integer, ClassifierInstanceEvaluation> classifierInstanceEvaluations) {
		this.classifierInstanceEvaluations = classifierInstanceEvaluations;
	}
	
}
