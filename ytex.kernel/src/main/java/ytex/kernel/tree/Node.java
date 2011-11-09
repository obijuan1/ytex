package ytex.kernel.tree;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "Node [type=" + type + ", value=" + value + "]";
	}

	private String type;
	private Map<String, Serializable> value;
	private List<Node> children = new LinkedList<Node>();

	/**
	 * Caching the norm externally, e.g. in EHCache involves too much additional
	 * overhead. Therefore, save the norm in this object. This shouldn't cause
	 * problems in a multi-threaded environment, as long as the kernel is the
	 * same - the value of the norm will be the same across evaluations.
	 */
	private transient Double norm;

	public Double getNorm() {
		return norm;
	}

	public void setNorm(Double norm) {
		this.norm = norm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Serializable> getValue() {
		return value;
	}

	public void setValue(Map<String, Serializable> value) {
		this.value = value;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
