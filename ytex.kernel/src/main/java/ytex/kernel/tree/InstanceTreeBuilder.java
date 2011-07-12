package ytex.kernel.tree;

import java.io.IOException;
import java.util.Map;

public interface InstanceTreeBuilder {

	/**
	 * Generate trees from the results of a sorted query
	 * 
	 */
	public Map<Integer, Node> loadInstanceTrees(TreeMappingInfo mappingInfo);

	public abstract void serializeInstanceTrees(TreeMappingInfo mappingInfo, String filename)
			throws IOException;

	public abstract Map<Integer, Node> loadInstanceTrees(String filename) throws IOException,
			ClassNotFoundException;

}