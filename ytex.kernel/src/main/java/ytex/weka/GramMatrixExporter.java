package ytex.weka;

import java.io.IOException;

public interface GramMatrixExporter {

	public abstract void exportGramMatrix(String propertyFile)
			throws IOException;

}