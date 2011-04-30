package ytex.kernel.model;

import java.io.Serializable;

public abstract class KernelEvaluation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int kernelEvaluationId;
	private String name;
	private String label;
	private String experiment;
	private int foldId;
	
	public int getKernelEvaluationId() {
		return kernelEvaluationId;
	}
	public void setKernelEvaluationId(int kernelEvaluationId) {
		this.kernelEvaluationId = kernelEvaluationId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getExperiment() {
		return experiment;
	}
	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}
	public int getFoldId() {
		return foldId;
	}
	public void setFoldId(int foldId) {
		this.foldId = foldId;
	}
}
