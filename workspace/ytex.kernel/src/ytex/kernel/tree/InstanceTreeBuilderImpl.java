package ytex.kernel.tree;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class InstanceTreeBuilderImpl implements InstanceTreeBuilder {
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	private Node nodeFromRow(NodeMappingInfo nodeInfo,
			Map<String, Object> nodeValues) {
		Node n = null;
		Map<String, Serializable> values = new HashMap<String, Serializable>(
				nodeInfo.getValues().size());
		for (String valueName : nodeInfo.getValues()) {
			if (nodeValues.containsKey(valueName)
					&& nodeValues.get(valueName) != null) {
				values.put(valueName, (Serializable) nodeValues.get(valueName));
			}
		}
		// make sure there is something to put in
		if (!values.isEmpty()) {
			n = new Node();
			n.setType(nodeInfo.getNodeType());
			n.setValue(values);
		}
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.tree.InstanceTreeBuilder#loadInstanceTrees(java.util.List,
	 * java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public Map<Integer, Node> loadInstanceTrees(
			List<NodeMappingInfo> nodeTypes, String instanceIDField,
			String query, Map<String, Object> queryArgs) {
		Node[] currentPath = new Node[nodeTypes.size()];
		Map<Integer, Node> instanceMap = new HashMap<Integer, Node>();
		List<Map<String, Object>> rowData = simpleJdbcTemplate.queryForList(
				query, queryArgs);
		for (Map<String, Object> row : rowData) {
			for (int i = 0; i < nodeTypes.size(); i++) {
				Node newNode = this.nodeFromRow(nodeTypes.get(i), row);
				if (newNode != null) {
					if (!newNode.equals(currentPath[i])) {
						if (i > 0) {
							// add the node to the parent
							currentPath[i-1].getChildren().add(newNode);
						} else {
							// this is a new root, i.e. a new instance
							// add it to the instance map
							instanceMap.put((Integer) row.get(instanceIDField),
									newNode);
						}
						// put the new node in the path
						// we don't really care about nodes 'after' this one in
						// the path list
						// because we only add to parents, not to children
						currentPath[i] = newNode;
					}
				}
			}
		}
		return instanceMap;
	}

}
