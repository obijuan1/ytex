package ytex.kernel.model;

import java.io.Serializable;
import java.util.Set;

public class CrossValidationFold implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int crossValidationFoldId;
	public int getCrossValidationFoldId() {
		return crossValidationFoldId;
	}
	public void setCrossValidationFoldId(int crossValidationFoldId) {
		this.crossValidationFoldId = crossValidationFoldId;
	}
	String name;
	String label;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	Integer run;
	Integer fold;
	Set<Integer> instanceIds;

	public Set<Integer> getInstanceIds() {
		return instanceIds;
	}
	public void setInstanceIds(Set<Integer> instanceIds) {
		this.instanceIds = instanceIds;
	}
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
	public CrossValidationFold(String name, String label, Integer run, Integer fold,
			Set<Integer> instanceIds) {
		super();
		this.name = name;
		this.run = run;
		this.fold = fold;
		this.label = label;
		this.instanceIds = instanceIds;
	}
	public CrossValidationFold() {
		super();
	}
}
