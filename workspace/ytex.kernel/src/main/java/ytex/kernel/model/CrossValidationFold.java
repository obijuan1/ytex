package ytex.kernel.model;

import java.io.Serializable;
import java.util.Set;

import ytex.dao.DBUtil;

public class CrossValidationFold implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String corpusName;
	private int crossValidationFoldId;
	private int fold = 0;
	private Set<CrossValidationFoldInstance> instanceIds;

	private String label = DBUtil.getEmptyString();

	private int run = 0;
	private String splitName = DBUtil.getEmptyString();

	public CrossValidationFold() {
		super();
	}

	public CrossValidationFold(String name, String splitName, String label, Integer run,
			Integer fold, Set<CrossValidationFoldInstance> instanceIds) {
		super();
		this.setCorpusName(name);
		this.setSplitName(splitName);
		this.setLabel(label);
		this.setRun(run);
		this.setFold(fold);
		this.instanceIds = instanceIds;
	}


	public String getCorpusName() {
		return corpusName;
	}

	//
	// /**
	// * is this the training or test fold?
	// * @return
	// */
	// public boolean isTrain() {
	// return train;
	// }
	// public void setTrain(boolean train) {
	// this.train = train;
	// }
	public int getCrossValidationFoldId() {
		return crossValidationFoldId;
	}

	public int getFold() {
		return fold;
	}

	public Set<CrossValidationFoldInstance> getInstanceIds() {
		return instanceIds;
	}

	public String getLabel() {
		return label;
	}

	public int getRun() {
		return run;
	}

	public String getSplitName() {
		return splitName;
	}

	public void setCorpusName(String name) {
		this.corpusName = name;
	}

	public void setCrossValidationFoldId(int crossValidationFoldId) {
		this.crossValidationFoldId = crossValidationFoldId;
	}

	public void setFold(int fold) {
		this.fold = fold;
	}

	public void setInstanceIds(Set<CrossValidationFoldInstance> instanceIds) {
		this.instanceIds = instanceIds;
	}

	public void setLabel(String label) {
		this.label = DBUtil.nullToEmptyString(label);
	}

	public void setRun(int run) {
		this.run = run;
	}

	public void setSplitName(String splitName) {
		this.splitName = DBUtil.nullToEmptyString(splitName);
	}

}
