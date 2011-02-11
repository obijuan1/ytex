package ytex.kernel;

import java.io.IOException;

public interface GramMatrixExporter {

	public abstract void exportGramMatrix(String propertyFile)
			throws IOException;

}