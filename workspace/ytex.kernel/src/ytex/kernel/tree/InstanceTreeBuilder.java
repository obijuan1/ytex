package ytex.kernel.tree;

import java.util.Map;

public interface InstanceTreeBuilder {

	/**
	 * Generate trees from the results of a sorted query
	 * 
	 */
	public Map<Integer, Node> loadInstanceTrees(TreeMappingInfo mappingInfo);

}