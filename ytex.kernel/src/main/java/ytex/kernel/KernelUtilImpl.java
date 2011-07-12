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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.KernelEvaluationDao;
import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.model.KernelEvaluationInstance;

public class KernelUtilImpl implements KernelUtil {
	private JdbcTemplate jdbcTemplate = null;
	private PlatformTransactionManager transactionManager;
	private KernelEvaluationDao kernelEvaluationDao = null;

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
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
				int instanceId = rs.getInt(1);
				String className = rs.getString(2);
				if (rs.getMetaData().getColumnCount() >= 3)
					train = rs.getBoolean(3);
				if (rs.getMetaData().getColumnCount() >= 4) {
					label = rs.getString(4);
					if(label == null)
						label = "";
				}
				if (rs.getMetaData().getColumnCount() >= 5)
					fold = rs.getInt(5);
				if (rs.getMetaData().getColumnCount() >= 6)
					run = rs.getInt(6);
				// get runs for label
				SortedMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>> runToInstanceMap = instanceLabel
						.getLabelToInstanceMap().get(label);
				if (runToInstanceMap == null) {
					runToInstanceMap = new TreeMap<Integer, SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>>();
					instanceLabel.getLabelToInstanceMap().put(label,
							runToInstanceMap);
				}
				// get folds for run
				SortedMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>> foldToInstanceMap = runToInstanceMap
						.get(run);
				if (foldToInstanceMap == null) {
					foldToInstanceMap = new TreeMap<Integer, SortedMap<Boolean, SortedMap<Integer, String>>>();
					runToInstanceMap.put(run, foldToInstanceMap);
				}
				// get train/test set for fold
				SortedMap<Boolean, SortedMap<Integer, String>> ttToClassMap = foldToInstanceMap
						.get(fold);
				if (ttToClassMap == null) {
					ttToClassMap = new TreeMap<Boolean, SortedMap<Integer, String>>();
					foldToInstanceMap.put(fold, ttToClassMap);
				}
				// get instances for train/test set
				SortedMap<Integer, String> instanceToClassMap = ttToClassMap
						.get(train);
				if (instanceToClassMap == null) {
					instanceToClassMap = new TreeMap<Integer, String>();
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

	@Override
	public void fillGramMatrix(
			final KernelEvaluation kernelEvaluation,
			final SortedSet<Integer> trainInstanceLabelMap,
			// final SortedMap<Integer, Map<String, Integer>>
			// trainInstanceLabelMap,
			final double[][] trainGramMatrix,
			// final SortedMap<Integer, Map<String, Integer>>
			// testInstanceLabelMap,
			final SortedSet<Integer> testInstanceLabelMap,
			final double[][] testGramMatrix) {
		// final Set<String> kernelEvaluationNames = new HashSet<String>(1);
		// kernelEvaluationNames.add(name);
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
			t.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			t.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					List<KernelEvaluationInstance> kevals = getKernelEvaluationDao()
							.getAllKernelEvaluationsForInstance(
									kernelEvaluation, instanceId);
					for (KernelEvaluationInstance keval : kevals) {
						// determine the index of the instance
						// the index could be in the training or test matrix
						Integer indexOtherTrain = null;
						Integer indexOtherTest = null;
						int instanceIdOther = instanceId != keval
								.getInstanceId1() ? keval.getInstanceId1()
								: keval.getInstanceId2();
						// look in training set for the instance id
						indexOtherTrain = trainInstanceToIndexMap
								.get(instanceIdOther);
						// wasn't there - look in test set
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
		// put 1's in the diagonal of the training gram matrix
		for (int i = 0; i < trainGramMatrix.length; i++) {
			if (trainGramMatrix[i][i] == 0)
				trainGramMatrix[i][i] = 1;
		}
	}

	private Map<Integer, Integer> createInstanceIdToIndexMap(
			SortedSet<Integer> instanceIDs) {
		Map<Integer, Integer> instanceIdToIndexMap = new HashMap<Integer, Integer>(
				instanceIDs.size());
		int i = 0;
		for (Integer instanceId : instanceIDs) {
			instanceIdToIndexMap.put(instanceId, i);
			i++;
		}
		return instanceIdToIndexMap;
	}

}
