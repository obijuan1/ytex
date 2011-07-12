package ytex.kernel.model;

import java.io.Serializable;

import ytex.kernel.DBUtil;

public class KernelEvaluation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int kernelEvaluationId;
	private String corpusName;
	private String label = DBUtil.getEmptyString();
	private String experiment = DBUtil.getEmptyString();
	private int foldId;
	private double param1 = 0;
	private String param2 = DBUtil.getEmptyString();

	public double getParam1() {
		return param1;
	}

	public void setParam1(double param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public int getKernelEvaluationId() {
		return kernelEvaluationId;
	}

	public void setKernelEvaluationId(int kernelEvaluationId) {
		this.kernelEvaluationId = kernelEvaluationId;
	}

	public String getCorpusName() {
		return corpusName;
	}

	public void setCorpusName(String name) {
		this.corpusName = name;
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
