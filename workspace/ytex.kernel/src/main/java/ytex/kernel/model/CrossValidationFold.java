package ytex.kernel.model;

import java.io.Serializable;
import java.util.Set;

public class CrossValidationFold implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int crossValidationFoldId;
	private String name;
	private String label;
	private Integer run;
	private Integer fold;
	private Set<CrossValidationFoldInstance> instanceIds;

	// private Set<Integer> instanceIds;
	// private boolean train;
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

	public void setCrossValidationFoldId(int crossValidationFoldId) {
		this.crossValidationFoldId = crossValidationFoldId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set<CrossValidationFoldInstance> getInstanceIds() {
		return instanceIds;
	}

	public void setInstanceIds(Set<CrossValidationFoldInstance> instanceIds) {
		this.instanceIds = instanceIds;
	}

	// public Set<Integer> getInstanceIds() {
	// return instanceIds;
	// }
	// public void setInstanceIds(Set<Integer> instanceIds) {
	// this.instanceIds = instanceIds;
	// }
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRun() {
		return run;
	}

	public void setRun(Integer run) {
		this.run = run;
	}

	public Integer getFold() {
		return fold;
	}

	public void setFold(Integer fold) {
		this.fold = fold;
	}

	// boolean train, Set<Integer> instanceIds) {
	public CrossValidationFold(String name, String label, Integer run,
			Integer fold, Set<CrossValidationFoldInstance> instanceIds) {
		super();
		this.name = name;
		this.run = run;
		this.fold = fold;
		this.label = label;
		// this.train = train;
		this.instanceIds = instanceIds;
	}

	public CrossValidationFold() {
		super();
	}
}
