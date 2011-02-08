package ytex.kernel.tree;

import java.util.List;
import java.util.Map;

public interface InstanceTreeBuilder {

	/**
	 * Generate trees from the results of a sorted query
	 * @param nodeTypes
	 * @param instanceIDField
	 * @param query
	 * @param queryArgs
	 * @return
	 */
	public abstract Map<Integer, Node> loadInstanceTrees(
			List<NodeMappingInfo> nodeTypes, String instanceIDField,
			String query, Map<String, Object> queryArgs);

}