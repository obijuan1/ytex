package ytex.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ytex.kernel.FileUtil;
import ytex.kernel.InfoGainEvaluatorImpl;
import ytex.kernel.InstanceData;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.KernelUtil;
import ytex.kernel.dao.KernelEvaluationDao;
import ytex.kernel.model.KernelEvaluation;
import ytex.sparsematrix.InstanceDataExporter;

public class RGramMatrixExporterImpl implements RGramMatrixExporter {
	@SuppressWarnings("static-access")
	public static void main(String args[]) throws IOException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("prop")
				.hasArg()
				.isRequired()
				.withDescription(
						"property file with queries and other kernel parameters")
				.create("prop"));
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);
			RGramMatrixExporter exporter = (RGramMatrixExporter) KernelContextHolder
					.getApplicationContext().getBean(RGramMatrixExporter.class);
			exporter.exportGramMatrix(FileUtil.loadProperties(
					line.getOptionValue("prop"), true));
		} catch (ParseException pe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java " + InfoGainEvaluatorImpl.class.getName()
					+ " calculate infogain for each feature", options);
		}
	}

	private InstanceDataExporter instanceDataExporter;
	private KernelEvaluationDao kernelEvaluationDao;

	private KernelUtil kernelUtil;

	private void exportGramMatrices(String name, String experiment,
			double param1, String param2, String outdir,
			InstanceData instanceData) throws IOException {
		for (String label : instanceData.getLabelToInstanceMap().keySet()) {
			for (int run : instanceData.getLabelToInstanceMap().get(label)
					.keySet()) {
				exportLabel(name, experiment, outdir, instanceData, label, run,
						param1, param2);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.R.RGramMatrixExporter#exportGramMatrix(java.util.Properties)
	 */
	@Override
	public void exportGramMatrix(Properties props) throws IOException {
		String name = props.getProperty("ytex.corpusName");
		String experiment = props.getProperty("ytex.experiment");
		String param2 = props.getProperty("ytex.param2");
		double param1 = Double.parseDouble(props.getProperty("ytex.param1", "0"));
		InstanceData instanceData = this.getKernelUtil().loadInstances(
				props.getProperty("instanceClassQuery"));
		String outdir = props.getProperty("outdir");
		if (outdir == null || outdir.length() == 0)
			outdir = ".";
		exportGramMatrices(name, experiment, param1, param2, outdir,
				instanceData);
	}

	private void exportLabel(String name, String experiment, String outdir,
			InstanceData instanceData, String label, int run, double param1,
			String param2) throws IOException {
		SortedSet<Integer> instanceIds = getAllInstanceIdsForLabel(
				instanceData, label);
		double[][] gramMatrix = new double[instanceIds.size()][instanceIds
				.size()];
		KernelEvaluation kernelEval = this.kernelEvaluationDao.getKernelEval(
				name, experiment, label, 0, param1, param2);
		kernelUtil.fillGramMatrix(kernelEval, instanceIds, gramMatrix, null,
				null);
		outputInstanceData(instanceData, label, outdir);
		outputGramMatrix(kernelEval, gramMatrix, instanceIds,
				FileUtil.getDataFilePrefix(outdir, label, 0, 0, null));

	}

	/**
	 * get all instance ids for the specified label
	 * 
	 * @param instanceData
	 * @param label
	 * @return
	 */
	private SortedSet<Integer> getAllInstanceIdsForLabel(
			InstanceData instanceData, String label) {
		SortedSet<Integer> instanceIds = new TreeSet<Integer>();
		for (int run : instanceData.getLabelToInstanceMap().get(label).keySet()) {
			for (int fold : instanceData.getLabelToInstanceMap().get(label)
					.get(run).keySet()) {
				for (SortedMap<Integer, String> instanceLabelMap : instanceData
						.getLabelToInstanceMap().get(label).get(run).get(fold)
						.values()) {
					instanceIds.addAll(instanceLabelMap.keySet());
				}
			}
		}
		return instanceIds;
	}

	public InstanceDataExporter getInstanceDataExporter() {
		return instanceDataExporter;
	}

	public KernelEvaluationDao getKernelEvaluationDao() {
		return kernelEvaluationDao;
	}

	public KernelUtil getKernelUtil() {
		return kernelUtil;
	}

	private void outputGramMatrix(KernelEvaluation kernelEval,
			double[][] gramMatrix, SortedSet<Integer> instanceIds,
			String dataFilePrefix) throws IOException {
		BufferedWriter w = null;
		BufferedWriter wId = null;
		try {
			w = new BufferedWriter(new FileWriter(dataFilePrefix + "data.txt"));
			wId = new BufferedWriter(new FileWriter(dataFilePrefix
					+ "instance_id.txt"));
			Integer instanceIdArray[] = instanceIds.toArray(new Integer[] {});
			// write colnames
			for (int h = 0; h < instanceIdArray.length; h++) {
				// w.write("\"");
				// w.write(Integer.toString(instanceIdArray[h]));
				// w.write("\" ");
				wId.write(Integer.toString(instanceIdArray[h]));
				wId.write("\n");
			}
			// w.write("\n");
			// write t
			for (int i = 0; i < instanceIdArray.length; i++) {
				// // write rowname
				// w.write("\"");
				// w.write(Integer.toString(instanceIdArray[i]));
				// w.write("\" ");
				// write line from gram matrix
				for (int j = 0; j < instanceIdArray.length; j++) {
					w.write(Double.toString(gramMatrix[i][j]));
					w.write(" ");
				}
				w.write("\n");
			}
		} finally {
			if (w != null) {
				w.close();
			}
			if (wId != null) {
				wId.close();
			}
		}
	}

	private void outputInstanceData(InstanceData instanceData, String label, String outdir)
			throws IOException {
		this.instanceDataExporter.outputInstanceData(instanceData,
				FileUtil.getFoldFilePrefix(outdir, label, 0, 0)
						+ "instance.txt");
	}

	// BufferedWriter bw = null;
	// try {
	// StringWriter w = new StringWriter();
	// boolean includeLabel = false;
	// boolean includeRun = false;
	// boolean includeFold = false;
	// boolean includeTrain = false;
	// for (String label : instanceData.getLabelToInstanceMap().keySet()) {
	// for (int run : instanceData.getLabelToInstanceMap().get(label)
	// .keySet()) {
	// for (int fold : instanceData.getLabelToInstanceMap()
	// .get(label).get(run).keySet()) {
	// for (boolean train : instanceData
	// .getLabelToInstanceMap().get(label).get(run)
	// .get(fold).keySet()) {
	// for (Map.Entry<Integer, String> instanceClass : instanceData
	// .getLabelToInstanceMap().get(label)
	// .get(run).get(fold).get(train).entrySet()) {
	// if (label.length() > 0) {
	// includeLabel = true;
	// w.write("\"");
	// w.write(label);
	// w.write("\" ");
	// }
	// if (run > 0) {
	// includeRun = true;
	// w.write(Integer.toString(run));
	// w.write(" ");
	// }
	// if (fold > 0) {
	// includeFold = true;
	// w.write(Integer.toString(fold));
	// w.write(" ");
	// }
	// if (instanceData.getLabelToInstanceMap()
	// .get(label).get(run).size() > 1) {
	// includeTrain = true;
	// w.write(train ? "1" : "0");
	// w.write(" ");
	// }
	// w.write(Integer.toString(instanceClass.getKey()));
	// w.write(" ");
	// w.write("\"");
	// w.write(instanceClass.getValue());
	// w.write("\"\n");
	// }
	// }
	// }
	// }
	// }
	// bw = new BufferedWriter(new FileWriter(outdir + "/instance.txt"));
	// // write colnames
	// if (includeLabel)
	// bw.write("\"label\" ");
	// if (includeRun)
	// bw.write("\"run\" ");
	// if (includeFold)
	// bw.write("\"fold\" ");
	// if (includeTrain)
	// bw.write("\"train\" ");
	// bw.write("\"instance_id\" \"class\"\n");
	// // write the rest of the data
	// bw.write(w.toString());
	// } finally {
	// if (bw != null) {
	// bw.close();
	// }
	// }

	// }

	public void setInstanceDataExporter(
			InstanceDataExporter instanceDataExporter) {
		this.instanceDataExporter = instanceDataExporter;
	}

	public void setKernelEvaluationDao(KernelEvaluationDao kernelEvaluationDao) {
		this.kernelEvaluationDao = kernelEvaluationDao;
	}

	public void setKernelUtil(KernelUtil kernelUtil) {
		this.kernelUtil = kernelUtil;
	}

	// private void exportGramMatrices(String name, String experiment,
	// String outdir, String instanceQuery) throws IOException {
	// SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, Integer>>>
	// instanceFolds = new TreeMap<Integer, SortedMap<Boolean,
	// SortedMap<Integer, Integer>>>();
	// SortedMap<String, SortedMap<Integer, String>> instanceLabels = new
	// TreeMap<String, SortedMap<Integer, String>>();
	//
	// }
	//
	// private void exportLabel(String name, String experiment, String outdir) {
	// }
	//
	// private static class InstanceFoldData {
	// SortedMap<Boolean, SortedMap<Integer, Integer>> folds;
	//
	// public void addEntry(boolean train, int fold, int run) {
	// SortedMap<Integer, Integer> foldToRun = folds.get(train);
	// if (foldToRun == null) {
	// if (fold != 0) {
	// foldToRun = new TreeMap<Integer, Integer>();
	// foldToRun.put(fold, run);
	// }
	// }
	// folds.put(train, foldToRun);
	// }
	// }
	//
	// private void loadInstanceData(
	// String strQuery,
	// final SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, Integer>>>
	// instanceFolds,
	// final SortedMap<String, SortedMap<Integer, String>> instanceLabels) {
	// jdbcTemplate.query(strQuery, new RowCallbackHandler() {
	//
	// @Override
	// public void processRow(ResultSet rs) throws SQLException {
	// String label = "";
	// int run = 0;
	// int fold = 0;
	// Boolean train = null;
	// int instanceId = rs.getInt(1);
	// String className = rs.getString(2);
	// if (rs.getMetaData().getColumnCount() >= 3)
	// train = rs.getBoolean(3);
	// if (rs.getMetaData().getColumnCount() >= 4)
	// label = rs.getString(4);
	// if (rs.getMetaData().getColumnCount() >= 5)
	// fold = rs.getInt(5);
	// if (rs.getMetaData().getColumnCount() >= 6)
	// run = rs.getInt(6);
	// // set instance className for label
	// SortedMap<Integer, String> instClassName = instanceLabels
	// .get(label);
	// if (instClassName == null) {
	// instClassName = new TreeMap<Integer, String>();
	// instClassName.put(instanceId, labels);
	// }
	// labels.put(label, className);
	// // set fold data
	// if (train != null) {
	// // we split into train/test - save this in the instanceFolds
	// SortedMap<Boolean, SortedMap<Integer, Integer>> folds = instanceFolds
	// .get(instanceId);
	// if (folds == null) {
	// folds = new TreeMap<Boolean, SortedMap<Integer, Integer>>();
	// instanceFolds.put(instanceId, folds);
	// }
	// // we split into folds / runs
	// SortedMap<Integer, Integer> foldToRun = folds.get(train);
	// if (foldToRun == null) {
	// if (fold != 0) {
	// foldToRun = new TreeMap<Integer, Integer>();
	// foldToRun.put(fold, run);
	// }
	// }
	// // add train/test flag
	// // foldToRun is null if we don't have any folds
	// folds.put(train, foldToRun);
	// }
	// }
	// });
	// }
}
