package ytex.kernel;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public interface SparseDataExporter {

	public abstract void exportData(String propertiesFile, String format)
			throws IOException, InvalidPropertiesFormatException;

	public abstract void exportData(Properties props,
			SparseDataFormatter formatter, BagOfWordsDecorator bDecorator)
			throws IOException;

}