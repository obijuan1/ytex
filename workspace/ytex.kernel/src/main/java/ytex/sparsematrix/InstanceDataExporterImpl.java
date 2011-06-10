package ytex.sparsematrix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import ytex.kernel.InstanceData;

/**
 * output the instance data
 * 
 * @author vijay
 * 
 */
public class InstanceDataExporterImpl implements InstanceDataExporter {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.sparsematrix.InstanceDataExporter#outputInstanceData(ytex.kernel
	 * .InstanceData, java.lang.String)
	 */
	@Override
	public void outputInstanceData(InstanceData instanceData, String filename)
			throws IOException {
		outputInstanceData(instanceData, filename, FIELD_DELIM, RECORD_DELIM,
				STRING_ESCAPE, INCLUDE_HEADER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.sparsematrix.InstanceDataExporter#outputInstanceData(ytex.kernel
	 * .InstanceData, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void outputInstanceData(InstanceData instanceData, String filename,
			String fieldDelim, String recordDelim, String stringEscape,
			boolean includeHeader) throws IOException {
		BufferedWriter bw = null;
		try {
			StringWriter w = new StringWriter();
			boolean includeLabel = false;
			boolean includeRun = false;
			boolean includeFold = false;
			boolean includeTrain = false;
			for (String label : instanceData.getLabelToInstanceMap().keySet()) {
				for (int run : instanceData.getLabelToInstanceMap().get(label)
						.keySet()) {
					for (int fold : instanceData.getLabelToInstanceMap()
							.get(label).get(run).keySet()) {
						for (boolean train : instanceData
								.getLabelToInstanceMap().get(label).get(run)
								.get(fold).keySet()) {
							for (Map.Entry<Integer, String> instanceClass : instanceData
									.getLabelToInstanceMap().get(label)
									.get(run).get(fold).get(train).entrySet()) {
								// write instance id
								w.write(Integer.toString(instanceClass.getKey()));
								w.write(fieldDelim);
								// write class
								appendString(instanceClass.getValue(),
										stringEscape, w);
								if (label.length() > 0) {
									includeLabel = true;
									w.write(fieldDelim);
									appendString(label, stringEscape, w);
								}
								if (run > 0) {
									includeRun = true;
									w.write(fieldDelim);
									w.write(Integer.toString(run));
								}
								if (fold > 0) {
									includeFold = true;
									w.write(fieldDelim);
									w.write(Integer.toString(fold));
								}
								if (instanceData.getLabelToInstanceMap()
										.get(label).get(run).size() > 1) {
									includeTrain = true;
									w.write(fieldDelim);
									w.write(train ? "1" : "0");
								}
								w.write(recordDelim);
							}
						}
					}
				}
			}
			bw = new BufferedWriter(new FileWriter(filename));
			if (includeHeader) {
				appendString("instance_id", stringEscape, bw);
				bw.write(fieldDelim);
				appendString("class", stringEscape, bw);
				// write colnames
				if (includeLabel) {
					bw.write(fieldDelim);
					appendString("label", stringEscape, bw);
				}
				if (includeRun) {
					bw.write(fieldDelim);
					appendString("run", stringEscape, bw);
				}
				if (includeFold) {
					bw.write(fieldDelim);
					appendString("fold", stringEscape, bw);
				}
				if (includeTrain) {
					bw.write(fieldDelim);
					appendString("train", stringEscape, bw);
				}
				bw.write(recordDelim);
			}
			// write the rest of the data
			bw.write(w.toString());
		} finally {
			if (bw != null) {
				bw.close();
			}
		}

	}

	private void appendString(String str, String stringEscape, Writer w)
			throws IOException {
		w.write(stringEscape);
		w.write(str);
		w.write(stringEscape);
	}

}
