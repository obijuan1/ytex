package ytex.kernel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.KernelEvaluationDao;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.model.KernelEvaluationInstance;

public class KernelUtilImpl implements KernelUtil {
	private static final Log log = LogFactory.getLog(KernelUtilImpl.class);
	private ClassifierEvaluationDao classifierEvaluationDao;

	private JdbcTemplate jdbcTemplate = null;

	private KernelEvaluationDao kernelEvaluationDao = null;
	private PlatformTransactionManager transactionManager;
	private FoldGenerator foldGenerator = null;

	public FoldGenerator getFoldGenerator() {
		return foldGenerator;
	}

	public void setFoldGenerator(FoldGenerator foldGenerator) {
		this.foldGenerator = foldGenerator;
	}

	private Map<Long, Integer> createInstanceIdToIndexMap(
			SortedSet<Long> instanceIDs) {
		Map<Long, Integer> instanceIdToIndexMap = new HashMap<Long, Integer>(
				instanceIDs.size());
		int i = 0;
		for (Long instanceId : instanceIDs) {
			instanceIdToIndexMap.put(instanceId, i);
			i++;
		}
		return instanceIdToIndexMap;
	}

	@Override
	public void fillGramMatrix(final KernelEvaluation kernelEvaluation,
			final SortedSet<Long> trainInstanceLabelMap,
			final double[][] trainGramMatrix) {
		// final Set<String> kernelEvaluationNames = new HashSet<String>(1);
		// kernelEvaluationNames.add(name);
		// prepare map of instance id to gram matrix index
		final Map<Long, Integer> trainInstanceToIndexMap = createInstanceIdToIndexMap(trainInstanceLabelMap);

		// iterate through the training instances
		for (Map.Entry<Long, Integer> instanceIdIndex : trainInstanceToIndexMap
				.entrySet()) {
			// index of this instance
			final int indexThis = instanceIdIndex.getValue();
			// id of this instance
			final long instanceId = instanceIdIndex.getKey();
			// get all kernel evaluations for this instance in a new transaction
			// don't want too many objects in hibernate session
			TransactionTemplate t = new TransactionTemplate(
					this.transactionManager);
			t.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			t.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					List<KernelEvaluationInstance> kevals = getKernelEvaluationDao()
							.getAllKernelEvaluationsForInstance(
									kernelEvaluation, instanceId);
					for (KernelEvaluationInstance keval : kevals) {
						// determine the index of the instance
						Integer indexOtherTrain = null;
						long instanceIdOther = instanceId != keval
								.getInstanceId1() ? keval.getInstanceId1()
								: keval.getInstanceId2();
						// look in training set for the instance id
						indexOtherTrain = trainInstanceToIndexMap
								.get(instanceIdOther);
						if (indexOtherTrain != null) {
							trainGramMatrix[indexThis][indexOtherTrain] = keval
									.getSimilarity();
							trainGramMatrix[indexOtherTrain][indexThis] = keval
									.getSimilarity();
						}
					}
					return null;
				}
			});
		}
		// put 1's in the diagonal of the training gram matrix
		for (int i = 0; i < trainGramMatrix.length; i++) {
			if (trainGramMatrix[i][i] == 0)
				trainGramMatrix[i][i] = 1;
		}
	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	public KernelEvaluationDao getKernelEvaluationDao() {
		return kernelEvaluationDao;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	@Override
	public double[][] loadGramMatrix(SortedSet<Long> instanceIds, String name,
			String splitName, String experiment, String label, int run,
			int fold, double param1, String param2) {
		int foldId = 0;
		double[][] gramMatrix = null;
		if (run != 0 && fold != 0) {
			CrossValidationFold f = this.classifierEvaluationDao
					.getCrossValidationFold(name, splitName, label, run, fold);
			if (f != null)
				foldId = f.getCrossValidationFoldId();
		}
		KernelEvaluation kernelEval = this.kernelEvaluationDao.getKernelEval(
				name, experiment, label, foldId, param1, param2);
		if (kernelEval == null) {
			log.warn("could not find kernelEvaluation.  name=" + name
					+ ", experiment=" + experiment + ", label=" + label
					+ ", fold=" + fold + ", run=" + run);
		} else {
			gramMatrix = new double[instanceIds.size()][instanceIds.size()];
			fillGramMatrix(kernelEval, instanceIds, gramMatrix);
		}
		return gramMatrix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.DataExporter#loadInstances(java.lang.String,
	 * java.util.SortedMap)
	 */
	@Override
	public InstanceData loadInstances(String strQuery) {
		final InstanceData instanceLabel = new InstanceData();
		jdbcTemplate.query(strQuery, new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String label = "";
				int run = 0;
				int fold = 0;
				boolean train = true;
				long instanceId = rs.getLong(1);
				String className = rs.getString(2);
				if (rs.getMetaData().getColumnCount() >= 3)
					train = rs.getBoolean(3);
				if (rs.getMetaData().getColumnCount() >= 4) {
					label = rs.getString(4);
					if (label == null)
						label = "";
				}
				if (rs.getMetaData().getColumnCount() >= 5)
					fold = rs.getInt(5);
				if (rs.getMetaData().getColumnCount() >= 6)
					run = rs.getInt(6);
				// get runs for label
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>> runToInstanceMap = instanceLabel
						.getLabelToInstanceMap().get(label);
				if (runToInstanceMap == null) {
					runToInstanceMap = new TreeMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>>();
					instanceLabel.getLabelToInstanceMap().put(label,
							runToInstanceMap);
				}
				// get folds for run
				SortedMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>> foldToInstanceMap = runToInstanceMap
						.get(run);
				if (foldToInstanceMap == null) {
					foldToInstanceMap = new TreeMap<Integer, SortedMap<Boolean, SortedMap<Long, String>>>();
					runToInstanceMap.put(run, foldToInstanceMap);
				}
				// get train/test set for fold
				SortedMap<Boolean, SortedMap<Long, String>> ttToClassMap = foldToInstanceMap
						.get(fold);
				if (ttToClassMap == null) {
					ttToClassMap = new TreeMap<Boolean, SortedMap<Long, String>>();
					foldToInstanceMap.put(fold, ttToClassMap);
				}
				// get instances for train/test set
				SortedMap<Long, String> instanceToClassMap = ttToClassMap
						.get(train);
				if (instanceToClassMap == null) {
					instanceToClassMap = new TreeMap<Long, String>();
					ttToClassMap.put(train, instanceToClassMap);
				}
				// set the instance class
				instanceToClassMap.put(instanceId, className);
				// add the class to the labelToClassMap
				SortedSet<String> labelClasses = instanceLabel
						.getLabelToClassMap().get(label);
				if (labelClasses == null) {
					labelClasses = new TreeSet<String>();
					instanceLabel.getLabelToClassMap().put(label, labelClasses);
				}
				if (!labelClasses.contains(className))
					labelClasses.add(className);
			}
		});
		return instanceLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.DataExporter#loadProperties(java.lang.String,
	 * java.util.Properties)
	 */
	@Override
	public void loadProperties(String propertyFile, Properties props)
			throws FileNotFoundException, IOException,
			InvalidPropertiesFormatException {
		InputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			if (propertyFile.endsWith(".xml"))
				props.loadFromXML(in);
			else
				props.load(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setKernelEvaluationDao(KernelEvaluationDao kernelEvaluationDao) {
		this.kernelEvaluationDao = kernelEvaluationDao;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public void generateFolds(InstanceData instanceLabel, Properties props) {
		int folds = Integer.parseInt(props.getProperty("folds"));
		int runs = Integer.parseInt(props.getProperty("runs", "1"));
		int minPerClass = Integer.parseInt(props
				.getProperty("minPerClass", "0"));
		Integer randomNumberSeed = props.containsKey("rand") ? Integer
				.parseInt(props.getProperty("rand")) : null;
		instanceLabel.setLabelToInstanceMap(foldGenerator.generateRuns(
				instanceLabel.getLabelToInstanceMap(), folds, minPerClass,
				randomNumberSeed, runs));
	}
}
