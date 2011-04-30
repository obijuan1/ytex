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
	String experiment;
	Integer fold;
	Integer run;
	String algorithm;
	String label;
	String options;
	byte[] model;
	Map<Integer, ClassifierInstanceEvaluation> classifierInstanceEvaluations = new HashMap<Integer, ClassifierInstanceEvaluation>();
	Map<Integer, ClassifierEvaluationIRStat> classifierIRStats = new HashMap<Integer, ClassifierEvaluationIRStat>();
	
	public Map<Integer, ClassifierEvaluationIRStat> getClassifierIRStats() {
		return classifierIRStats;
	}
	public void setClassifierIRStats(
			Map<Integer, ClassifierEvaluationIRStat> classifierIRStats) {
		this.classifierIRStats = classifierIRStats;
	}
	public String getExperiment() {
		return experiment;
	}
	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}	
	public byte[] getModel() {
		return model;
	}
	public void setModel(byte[] model) {
		this.model = model;
	}
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
	public Integer getFold() {
		return fold;
	}
	public void setFold(Integer fold) {
		this.fold = fold;
	}
	public Integer getRun() {
		return run;
	}
	public void setRun(Integer run) {
		this.run = run;
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
