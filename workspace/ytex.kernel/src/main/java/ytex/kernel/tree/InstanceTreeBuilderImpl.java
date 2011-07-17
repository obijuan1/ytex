package ytex.kernel.tree;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class InstanceTreeBuilderImpl implements InstanceTreeBuilder {
	static final Log log = LogFactory.getLog(InstanceTreeBuilderImpl.class);
	SimpleJdbcTemplate simpleJdbcTemplate;
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	Node nodeFromRow(NodeMappingInfo nodeInfo, Map<String, Object> nodeValues) {
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

	@SuppressWarnings("unchecked")
	@Override
	public Map<Integer, Node> loadInstanceTrees(String filename)
			throws IOException, ClassNotFoundException {
		ObjectInputStream os = null;
		try {
			os = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(filename)));
			return (Map<Integer, Node>) os.readObject();
		} finally {
			if (os != null)
				os.close();
		}
	}

	@Override
	public void serializeInstanceTrees(TreeMappingInfo mappingInfo,
			String filename) throws IOException {
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(filename)));
			os.writeObject(loadInstanceTrees(mappingInfo));
		} finally {
			if (os != null)
				os.close();
		}
	}

	public Map<Integer, Node> loadInstanceTrees(TreeMappingInfo mappingInfo) {
		Map<NodeKey, Node> nodeKeyMap = new HashMap<NodeKey, Node>();
		Map<Integer, Node> instanceMap = loadInstanceTrees(
				mappingInfo.getInstanceIDField(),
				mappingInfo.getInstanceQueryMappingInfo(), nodeKeyMap);
		if (mappingInfo.getNodeQueryMappingInfos() != null) {
			for (QueryMappingInfo qInfo : mappingInfo
					.getNodeQueryMappingInfos()) {
				this.addChildrenToNodes(nodeKeyMap, qInfo);
			}
		}
		return instanceMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.tree.InstanceTreeBuilder#loadInstanceTrees(java.util.List,
	 * java.lang.String, java.lang.String, java.util.Map)
	 */
	public Map<Integer, Node> loadInstanceTrees(String instanceIDField,
			QueryMappingInfo qInfo, Map<NodeKey, Node> nodeKeyMap) {
		Node[] currentPath = new Node[qInfo.getNodeTypes().size()];
		Map<Integer, Node> instanceMap = new HashMap<Integer, Node>();
		List<Map<String, Object>> rowData = simpleJdbcTemplate.queryForList(
				qInfo.getQuery(), qInfo.getQueryArgs());
		for (Map<String, Object> row : rowData) {
			for (int i = 0; i < qInfo.getNodeTypes().size(); i++) {
				Node newNode = this.nodeFromRow(qInfo.getNodeTypes().get(i),
						row);
				if (newNode != null) {
					if (!newNode.equals(currentPath[i])) {
						if (i > 0) {
							// add the node to the parent
							currentPath[i - 1].getChildren().add(newNode);
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
						if (nodeKeyMap != null)
							nodeKeyMap.put(new NodeKey(newNode), newNode);
					}
				}
			}
		}
		return instanceMap;
	}

	public void addChildrenToNodes(Map<NodeKey, Node> nodeKeyMap,
			QueryMappingInfo qInfo) {
		// run query
		List<Map<String, Object>> rowData = simpleJdbcTemplate.queryForList(
				qInfo.getQuery(), qInfo.getQueryArgs());
		// iterate through rows, adding nodes as children of existing nodes
		for (Map<String, Object> row : rowData) {
			// allocate array for holding node path corresponding to row
			Node[] currentPath = new Node[qInfo.getNodeTypes().size()];
			// get the root of this subtree - temporary node contains values
			Node parentTmp = nodeFromRow(qInfo.getNodeTypes().get(0), row);
			if (parentTmp != null) {
				// get the node from the tree that correponds to this node
				Node parent = nodeKeyMap.get(new NodeKey(parentTmp));
				if (parent == null) {
					if (log.isWarnEnabled()) {
						log.warn("couldn't find node for key: " + parentTmp);
					}
				} else {
					// found the parent - add the subtree
					currentPath[0] = parent;
					for (int i = 1; i < qInfo.getNodeTypes().size(); i++) {
						Node newNode = this.nodeFromRow(qInfo.getNodeTypes()
								.get(i), row);
						if (newNode != null) {
							if (!newNode.equals(currentPath[i])) {
								// null out everything after this index in the path
								Arrays.fill(currentPath, i,
										currentPath.length - 1, null);
								// add the node to the parent
								currentPath[i - 1].getChildren().add(newNode);
								// put the new node in the path
								// we don't really care about nodes 'after' this
								// one in the path list
								// because we only add to parents, not to
								// children
								currentPath[i] = newNode;
								if (nodeKeyMap != null)
									nodeKeyMap.put(new NodeKey(newNode),
											newNode);
							}
						}
					}
				}
			}
		}
	}

}
