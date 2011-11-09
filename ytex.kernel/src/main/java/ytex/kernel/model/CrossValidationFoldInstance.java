package ytex.kernel.model;

import java.io.Serializable;

public class CrossValidationFoldInstance implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long instanceId;
	private boolean train;
	public long getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}
	public boolean isTrain() {
		return train;
	}
	public void setTrain(boolean train) {
		this.train = train;
	}
	public CrossValidationFoldInstance(long instanceId, boolean train) {
		super();
		this.instanceId = instanceId;
		this.train = train;
	}
	public CrossValidationFoldInstance() {
		super();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (instanceId ^ (instanceId >>> 32));
		result = prime * result + (train ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CrossValidationFoldInstance other = (CrossValidationFoldInstance) obj;
		if (instanceId != other.instanceId)
			return false;
		if (train != other.train)
			return false;
		return true;
	}

	
}
