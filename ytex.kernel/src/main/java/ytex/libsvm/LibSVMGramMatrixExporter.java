package ytex.libsvm;

import java.io.IOException;
import java.util.Properties;

public interface LibSVMGramMatrixExporter {

	public abstract void exportGramMatrix(Properties props) throws IOException;

}