package ytex.kernel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Properties;

import ytex.kernel.model.ConceptGraph;

public interface CytoscapeHelper {


	public abstract boolean validateProps(Properties props);

	public abstract void exportNetwork(String filePrefix, Properties props) throws IOException;

	public abstract void exportSubtree(String conceptID, Properties props) throws IOException;

	void exportNetwork(ConceptGraph cg, String corpusName,
			String conceptGraphName, String conceptSetName,
			int leafChildrenDepth, BufferedWriter networkData,
			BufferedWriter nodeData) throws IOException;


}