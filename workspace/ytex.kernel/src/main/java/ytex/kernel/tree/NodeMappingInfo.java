package ytex.kernel.tree;

import java.util.Set;

public class NodeMappingInfo {
	private String nodeType;

	private Set<String> values;

	public NodeMappingInfo() {
		super();
	}

	public NodeMappingInfo(String nodeType, Set<String> values) {
		super();
		this.nodeType = nodeType;
		this.values = values;
	}

	public String getNodeType() {
		return nodeType;
	}

	public Set<String> getValues() {
		return values;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public void setValues(Set<String> values) {
		this.values = values;
	}
}
