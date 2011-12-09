package ytex.sparsematrix;

import java.io.IOException;

import ytex.kernel.InstanceData;

public interface InstanceDataExporter {

	public static final String FIELD_DELIM = "\t";
	public static final String RECORD_DELIM = "\n";
	public static final String STRING_ESCAPE = "";
	public static final boolean INCLUDE_HEADER = false; 

	public abstract void outputInstanceData(InstanceData instanceData,
			String filename) throws IOException;

	public abstract void outputInstanceData(InstanceData instanceData,
			String filename, String fieldDelim, String recordDelim,
			String stringEscape, boolean includeHeader) throws IOException;

}