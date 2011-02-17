package ytex.libsvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.KernelContextHolder;
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
 * <li>training_gram_[label].txt - for each class label, a symmetric gram matrix
 * for training instances
 * <li>training_instance_ids.txt - instance ids corresponding to rows of training gram
 * matrix
 * <li>test_gram_[label].txt - for each class label, a rectangular matrix of the
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
		String name = props.getProperty("kernel.name");
		String testInstanceQuery = props.getProperty("test.instance.query");
		String trainInstanceQuery = props.getProperty("train.instance.query");
		String outdir = props.getProperty("outdir");
		if(outdir == null || outdir.length() == 0)
			outdir = ".";
		exportGramMatrices(name, testInstanceQuery, trainInstanceQuery, outdir);
	}


	/**
	 * instantiate gram matrices, generate output files
	 * 
	 * @param name
	 * @param testInstanceQuery
	 * @param trainInstanceQuery
	 * @param outdir
	 * @throws IOException
	 */
	private void exportGramMatrices(String name, String testInstanceQuery,
			String trainInstanceQuery, String outdir) throws IOException {
		Set<String> labels = new HashSet<String>();
		SortedMap<Integer, Map<String, Integer>> trainInstanceLabelMap = libsvmUtil.loadClassLabels(
				trainInstanceQuery, labels);
		double[][] trainGramMatrix = new double[trainInstanceLabelMap.size()][trainInstanceLabelMap
				.size()];
		SortedMap<Integer, Map<String, Integer>> testInstanceLabelMap = null;
		double[][] testGramMatrix = null;
		if (testInstanceQuery != null) {
			testInstanceLabelMap = libsvmUtil.loadClassLabels(testInstanceQuery, labels);
			testGramMatrix = new double[testInstanceLabelMap.size()][trainInstanceLabelMap
					.size()];
		}
		fillGramMatrix(name, trainInstanceLabelMap, trainGramMatrix,
				testInstanceLabelMap, testGramMatrix);
		for (String label : labels) {
			outputGramMatrix(name, outdir, label, trainInstanceLabelMap,
					trainGramMatrix, "training");
			if (testGramMatrix != null) {
				outputGramMatrix(name, outdir, label, testInstanceLabelMap,
						testGramMatrix, "test");
			}
		}
		libsvmUtil.outputInstanceIds(outdir, trainInstanceLabelMap, "training");
		if (testInstanceLabelMap != null)
			libsvmUtil.outputInstanceIds(outdir, testInstanceLabelMap, "test");
	}


	private void outputGramMatrix(String name, String outdir, String label,
			SortedMap<Integer, Map<String, Integer>> instanceLabelMap,
			double[][] gramMatrix, String type) throws IOException {
		StringBuilder bFileName = new StringBuilder(outdir).append(
				File.separator).append(type).append(
				"_gram_").append(label).append(".txt");
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(bFileName.toString()));
			int rowIndex = 0;
			// the rows in the gramMatrix correspond to the entries in the
			// instanceLabelMap
			// both are in the same order
			for (Map.Entry<Integer, Map<String, Integer>> instanceLabels : instanceLabelMap
					.entrySet()) {
				// default the class Id to 0
				int classId = 0;
				if (instanceLabels.getValue() != null
						&& instanceLabels.getValue().containsKey(label)) {
					classId = instanceLabels.getValue().get(label);
				}
				// write class Id
				w.write(Integer.toString(classId));
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
			}
		} finally {
			if (w != null)
				w.close();
		}
	}

	private Map<Integer, Integer> createInstanceIdToIndexMap(
			SortedMap<Integer, Map<String, Integer>> instanceLabelMap) {
		Map<Integer, Integer> instanceIdToIndexMap = new HashMap<Integer, Integer>(
				instanceLabelMap.size());
		int i = 0;
		for (Integer instanceId : instanceLabelMap.keySet()) {
			instanceIdToIndexMap.put(instanceId, i);
			i++;
		}
		return instanceIdToIndexMap;
	}

	private void fillGramMatrix(
			final String name,
			final SortedMap<Integer, Map<String, Integer>> trainInstanceLabelMap,
			final double[][] trainGramMatrix,
			final SortedMap<Integer, Map<String, Integer>> testInstanceLabelMap,
			final double[][] testGramMatrix) {
		final Set<String> kernelEvaluationNames = new HashSet<String>(1);
		kernelEvaluationNames.add(name);
		// prepare map of instance id to gram matrix index
		final Map<Integer, Integer> trainInstanceToIndexMap = createInstanceIdToIndexMap(trainInstanceLabelMap);
		final Map<Integer, Integer> testInstanceToIndexMap = testInstanceLabelMap != null ? createInstanceIdToIndexMap(testInstanceLabelMap)
				: null;
		// iterate through the training instances
		for (Map.Entry<Integer, Integer> instanceIdIndex : trainInstanceToIndexMap
				.entrySet()) {
			// index of this instance
			final int indexThis = instanceIdIndex.getValue();
			// id of this instance
			final int instanceId = instanceIdIndex.getKey();
			// get all kernel evaluations for this instance in a new transaction
			// don't want too many objects in hibernate session
			TransactionTemplate t = new TransactionTemplate(
					this.transactionManager);
			t
					.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			t.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					List<KernelEvaluation> kevals = getKernelEvaluationDao()
					.getAllKernelEvaluationsForInstance(
							kernelEvaluationNames, instanceId); 
					for (KernelEvaluation keval : kevals) {
						// determine the index of the instance
						// the index could be in the training or test matrix
						Integer indexOtherTrain = null;
						Integer indexOtherTest = null;
						int instanceIdOther = instanceId != keval
								.getInstanceId1() ? keval.getInstanceId1()
								: keval.getInstanceId2();
						//look in training set for the instance id
						indexOtherTrain = trainInstanceToIndexMap
								.get(instanceIdOther);
						//wasn't there - look in test set
						if (indexOtherTrain == null
								&& testInstanceToIndexMap != null)
							indexOtherTest = testInstanceToIndexMap
									.get(instanceIdOther);
						if (indexOtherTrain != null) {
							trainGramMatrix[indexThis][indexOtherTrain] = keval
									.getSimilarity();
							trainGramMatrix[indexOtherTrain][indexThis] = keval
									.getSimilarity();
						} else if (indexOtherTest != null) {
							// test matrix is not symmetric
							// row corresponds to test instance id
							// column is kernel evaluation wrt to this instance
							testGramMatrix[indexOtherTest][indexThis] = keval
									.getSimilarity();
						}
					}
					return null;
				}
			});
		}
		//put 1's in the diagonal of the training gram matrix
		for(int i = 0; i< trainGramMatrix.length; i++) {
			if(trainGramMatrix[i][i] == 0)
				trainGramMatrix[i][i] = 1;
		}
	}

	public static void main(String args[]) throws IOException {
		Properties props = new Properties();
		InputStream propIS = null;
		try {
			propIS = new FileInputStream(args[0]);
			props.loadFromXML(propIS);
			LibSVMGramMatrixExporter exporter = (LibSVMGramMatrixExporter) KernelContextHolder
					.getApplicationContext()
					.getBean("libSVMGramMatrixExporter");
			exporter.exportGramMatrix(props);
		} finally {
			if (propIS != null) {
				propIS.close();
			}
		}
	}

}
