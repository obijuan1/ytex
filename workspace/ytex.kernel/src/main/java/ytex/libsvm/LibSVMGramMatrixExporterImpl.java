package ytex.libsvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import ytex.kernel.CorpusLabelEvaluator;
import ytex.kernel.FileUtil;
import ytex.kernel.InfoGainEvaluatorImpl;
import ytex.kernel.InstanceData;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.KernelUtil;
import ytex.kernel.dao.KernelEvaluationDao;
import ytex.kernel.model.KernelEvaluation;

/**
 * export gram matrix for libsvm. input properties file with following keys:
 * <p/>
 * <li>kernel.name name of kernel evaluation (corresponds to name column in
 * kernel_eval table) - required
 * <li>train.instance.query query to get test instance ids and their class
 * labels - required
 * <li>test.instance.query query to get test instance ids and their class labels
 * - optional. If not specified, will just do training gram matrix
 * <li>outdir directory where files will be place - optional defaults to current
 * directory
 * <p/>
 * Instance queries return the rows with 3 columns: [instance id (int)] [class
 * label (string)] [class index (int)] For single-class classification, only one
 * class label per instance. For multi-label classification, can have multiple
 * labels per instance. If no class index is specified for a given label, the
 * instance's class index for that label is set to 0.
 * <p/>
 * Output to outdir following files:
 * <li>training_data_[label].txt - for each class label, a symmetric gram matrix
 * for training instances
 * <li>training_instance_ids.txt - instance ids corresponding to rows of
 * training gram matrix
 * <li>test_data_[label].txt - for each class label, a rectangular matrix of the
 * test instances kernel evaluations wrt training instances
 * <li>test_instance_ids.txt - instance ids corresponding to rows of test gram
 * matrix
 * 
 * @author vijay
 */
public class LibSVMGramMatrixExporterImpl implements LibSVMGramMatrixExporter {
	private KernelEvaluationDao kernelEvaluationDao = null;
	private JdbcTemplate jdbcTemplate = null;
	private PlatformTransactionManager transactionManager;
	private LibSVMUtil libsvmUtil;
	private KernelUtil kernelUtil;

	public LibSVMUtil getLibsvmUtil() {
		return libsvmUtil;
	}

	public void setLibsvmUtil(LibSVMUtil libsvmUtil) {
		this.libsvmUtil = libsvmUtil;
	}

	public KernelEvaluationDao getKernelEvaluationDao() {
		return kernelEvaluationDao;
	}

	public void setKernelEvaluationDao(KernelEvaluationDao kernelEvaluationDao) {
		this.kernelEvaluationDao = kernelEvaluationDao;
	}

	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.libsvm.LibSVMGramMatrixExporter#exportGramMatrix(java.util.Properties
	 * )
	 */
	public void exportGramMatrix(Properties props) throws IOException {
		String name = props.getProperty("name");
		String experiment = props.getProperty("experiment");
		String param2 = props.getProperty("param2");
		double param1 = Double.parseDouble(props.getProperty("param1", "0"));
		InstanceData instanceData = this.getKernelUtil().loadInstances(
				props.getProperty("instanceClassQuery"));
		// String testInstanceQuery = props.getProperty("test.instance.query");
		// String trainInstanceQuery =
		// props.getProperty("train.instance.query");
		String outdir = props.getProperty("outdir");
		if (outdir == null || outdir.length() == 0)
			outdir = ".";
		exportGramMatrices(name, experiment, param1, param2, outdir,
				instanceData);
	}

	/**
	 * todo: this loads the gram matrix for each fold. this is very inefficient.
	 * Instead need to load gram matrix per label+experiment+fold,
	 * label+experiment, or per experiment based on configuration.
	 */
	private void exportGramMatrices(String name, String experiment,
			double param1, String param2, String outdir,
			InstanceData instanceData) throws IOException {
		for (String label : instanceData.getLabelToInstanceMap().keySet()) {
			for (int run : instanceData.getLabelToInstanceMap().get(label)
					.keySet()) {
				for (int fold : instanceData.getLabelToInstanceMap().get(label)
						.get(run).keySet()) {
					exportFold(name, experiment, outdir, instanceData, label,
							run, fold, param1, param2);
				}
			}
		}
	}

	private void exportFold(String name, String experiment, String outdir,
			InstanceData instanceData, String label, int run, int fold,
			double param1, String param2) throws IOException {
		SortedMap<Integer, String> trainInstanceLabelMap = instanceData
				.getLabelToInstanceMap().get(label).get(run).get(fold)
				.get(true);
		SortedMap<Integer, String> testInstanceLabelMap = instanceData
				.getLabelToInstanceMap().get(label).get(run).get(fold)
				.get(false);
		double[][] trainGramMatrix = new double[trainInstanceLabelMap.size()][trainInstanceLabelMap
				.size()];
		double[][] testGramMatrix = null;
		if (testInstanceLabelMap != null) {
			testGramMatrix = new double[testInstanceLabelMap.size()][trainInstanceLabelMap
					.size()];
		}
		KernelEvaluation kernelEval = this.kernelEvaluationDao.getKernelEval(
				name, experiment, label, 0, param1, param2);
		kernelUtil.fillGramMatrix(kernelEval, new TreeSet<Integer>(
				trainInstanceLabelMap.keySet()), trainGramMatrix,
				testInstanceLabelMap != null ? new TreeSet<Integer>(
						testInstanceLabelMap.keySet()) : null, testGramMatrix);
		outputGramMatrix(kernelEval, trainInstanceLabelMap, trainGramMatrix,
				FileUtil.getDataFilePrefix(outdir, label, run, fold,
						testInstanceLabelMap != null ? true : null));
		if (testGramMatrix != null) {
			outputGramMatrix(kernelEval, testInstanceLabelMap, testGramMatrix,
					FileUtil.getDataFilePrefix(outdir, label, run, fold, false));
		}
	}

	private void outputGramMatrix(KernelEvaluation kernelEval,
			SortedMap<Integer, String> instanceLabelMap, double[][] gramMatrix,
			String dataFilePrefix) throws IOException {
		StringBuilder bFileName = new StringBuilder(dataFilePrefix)
				.append("_data.txt");
		StringBuilder bIdFileName = new StringBuilder(dataFilePrefix)
				.append("_id.txt");
		BufferedWriter w = null;
		BufferedWriter wId = null;
		try {
			w = new BufferedWriter(new FileWriter(bFileName.toString()));
			wId = new BufferedWriter(new FileWriter(bIdFileName.toString()));
			int rowIndex = 0;
			// the rows in the gramMatrix correspond to the entries in the
			// instanceLabelMap
			// both are in the same order
			for (Map.Entry<Integer, String> instanceClass : instanceLabelMap
					.entrySet()) {
				// default the class Id to 0
				String classId = instanceClass.getValue();
				int instanceId = instanceClass.getKey();
				// write class Id
				w.write(classId);
				w.write("\t");
				// write row number - libsvm uses 1-based indexing
				w.write("0:");
				w.write(Integer.toString(rowIndex + 1));
				// write column entries
				for (int columnIndex = 0; columnIndex < gramMatrix[rowIndex].length; columnIndex++) {
					w.write("\t");
					// write column number
					w.write(Integer.toString(columnIndex + 1));
					w.write(":");
					// write value
					w.write(Double.toString(gramMatrix[rowIndex][columnIndex]));
				}
				w.newLine();
				// increment the row number
				rowIndex++;
				// write id file
				wId.write(Integer.toString(instanceId));
				wId.newLine();
			}
		} finally {
			if (w != null)
				w.close();
			if (wId != null)
				wId.close();
		}
	}

	// /**
	// * instantiate gram matrices, generate output files
	// *
	// * @param name
	// * @param testInstanceQuery
	// * @param trainInstanceQuery
	// * @param outdir
	// * @throws IOException
	// */
	// private void exportGramMatrices(String name, String testInstanceQuery,
	// String trainInstanceQuery, String outdir) throws IOException {
	// Set<String> labels = new HashSet<String>();
	// SortedMap<Integer, Map<String, Integer>> trainInstanceLabelMap =
	// libsvmUtil
	// .loadClassLabels(trainInstanceQuery, labels);
	// double[][] trainGramMatrix = new
	// double[trainInstanceLabelMap.size()][trainInstanceLabelMap
	// .size()];
	// SortedMap<Integer, Map<String, Integer>> testInstanceLabelMap = null;
	// double[][] testGramMatrix = null;
	// if (testInstanceQuery != null) {
	// testInstanceLabelMap = libsvmUtil.loadClassLabels(
	// testInstanceQuery, labels);
	// testGramMatrix = new
	// double[testInstanceLabelMap.size()][trainInstanceLabelMap
	// .size()];
	// }
	// // fillGramMatrix(name, trainInstanceLabelMap, trainGramMatrix,
	// // testInstanceLabelMap, testGramMatrix);
	// for (String label : labels) {
	// outputGramMatrix(name, outdir, label, trainInstanceLabelMap,
	// trainGramMatrix, "training");
	// if (testGramMatrix != null) {
	// outputGramMatrix(name, outdir, label, testInstanceLabelMap,
	// testGramMatrix, "test");
	// }
	// }
	// libsvmUtil.outputInstanceIds(outdir, trainInstanceLabelMap, "training");
	// if (testInstanceLabelMap != null)
	// libsvmUtil.outputInstanceIds(outdir, testInstanceLabelMap, "test");
	// }

	// private void outputGramMatrix(String name, String outdir, String label,
	// SortedMap<Integer, Map<String, Integer>> instanceLabelMap,
	// double[][] gramMatrix, String type) throws IOException {
	// StringBuilder bFileName = new StringBuilder(outdir)
	// .append(File.separator).append(type).append("_data_")
	// .append(label).append(".txt");
	// BufferedWriter w = null;
	// try {
	// w = new BufferedWriter(new FileWriter(bFileName.toString()));
	// int rowIndex = 0;
	// // the rows in the gramMatrix correspond to the entries in the
	// // instanceLabelMap
	// // both are in the same order
	// for (Map.Entry<Integer, Map<String, Integer>> instanceLabels :
	// instanceLabelMap
	// .entrySet()) {
	// // default the class Id to 0
	// int classId = 0;
	// if (instanceLabels.getValue() != null
	// && instanceLabels.getValue().containsKey(label)) {
	// classId = instanceLabels.getValue().get(label);
	// }
	// // write class Id
	// w.write(Integer.toString(classId));
	// w.write("\t");
	// // write row number - libsvm uses 1-based indexing
	// w.write("0:");
	// w.write(Integer.toString(rowIndex + 1));
	// // write column entries
	// for (int columnIndex = 0; columnIndex < gramMatrix[rowIndex].length;
	// columnIndex++) {
	// w.write("\t");
	// // write column number
	// w.write(Integer.toString(columnIndex + 1));
	// w.write(":");
	// // write value
	// w.write(Double.toString(gramMatrix[rowIndex][columnIndex]));
	// }
	// w.newLine();
	// // increment the row number
	// rowIndex++;
	// }
	// } finally {
	// if (w != null)
	// w.close();
	// }
	// }

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
			LibSVMGramMatrixExporter exporter = (LibSVMGramMatrixExporter) KernelContextHolder
					.getApplicationContext()
					.getBean("libSVMGramMatrixExporter");
			exporter.exportGramMatrix(FileUtil.loadProperties(
					line.getOptionValue("prop"), true));
		} catch (ParseException pe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java " + InfoGainEvaluatorImpl.class.getName()
					+ " calculate infogain for each feature", options);
		}
	}

	public void setKernelUtil(KernelUtil kernelUtil) {
		this.kernelUtil = kernelUtil;
	}

	public KernelUtil getKernelUtil() {
		return kernelUtil;
	}

}
