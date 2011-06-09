package ytex.kernel;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public interface SparseDataExporter {
	
	public enum ScopeEnum {
		LABEL("label"),
		FOLD("fold");
		private String scope;
		ScopeEnum(String scope) {
			this.scope = scope;
		}
		public String getScope() {
			return scope;
		}
	}

	public abstract void exportData(String propertiesFile, String format)
			throws IOException, InvalidPropertiesFormatException;

	public abstract void exportData(Properties props,
			SparseDataFormatter formatter, BagOfWordsDecorator bDecorator)
			throws IOException;

}