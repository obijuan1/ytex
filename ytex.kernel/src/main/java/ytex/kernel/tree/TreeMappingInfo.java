package ytex.kernel.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeMappingInfo {
	String instanceIDField;
	QueryMappingInfo instanceQueryMappingInfo;
	List<QueryMappingInfo> nodeQueryMappingInfos = new ArrayList<QueryMappingInfo>();
	String prepareScript;
	String prepareScriptStatementDelimiter = ";";


	public String getPrepareScript() {
		return prepareScript;
	}

	public void setPrepareScript(String prepareScript) {
		this.prepareScript = prepareScript;
	}

	public String getPrepareScriptStatementDelimiter() {
		return prepareScriptStatementDelimiter;
	}

	public void setPrepareScriptStatementDelimiter(
			String prepareScriptStatementDelimiter) {
		this.prepareScriptStatementDelimiter = prepareScriptStatementDelimiter;
	}

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
