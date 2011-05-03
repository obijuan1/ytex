package ytex.R;

import java.io.IOException;
import java.util.Properties;

public interface RGramMatrixExporter {

	public abstract void exportGramMatrix(Properties props) throws IOException;

}