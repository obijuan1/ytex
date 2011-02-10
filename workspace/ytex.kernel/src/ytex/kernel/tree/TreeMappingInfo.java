package ytex.kernel.tree;

import java.util.List;

public class TreeMappingInfo {
	String instanceIDField;
	QueryMappingInfo instanceQueryMappingInfo;
	List<QueryMappingInfo> nodeQueryMappingInfos;
	public String getInstanceIDField() {
		return instanceIDField;
	}
	public void setInstanceIDField(String instanceIDField) {
		this.instanceIDField = instanceIDField;
	}
	public QueryMappingInfo getInstanceQueryMappingInfo() {
		return instanceQueryMappingInfo;
	}
	public void setInstanceQueryMappingInfo(
			QueryMappingInfo instanceQueryMappingInfo) {
		this.instanceQueryMappingInfo = instanceQueryMappingInfo;
	}
	public List<QueryMappingInfo> getNodeQueryMappingInfos() {
		return nodeQueryMappingInfos;
	}
	public void setNodeQueryMappingInfos(
			List<QueryMappingInfo> nodeQueryMappingInfos) {
		this.nodeQueryMappingInfos = nodeQueryMappingInfos;
	}
}
