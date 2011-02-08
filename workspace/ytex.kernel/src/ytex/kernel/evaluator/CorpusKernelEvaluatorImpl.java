package ytex.kernel.evaluator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.KernelEvaluationDao;
import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.tree.InstanceTreeBuilder;
import ytex.kernel.tree.Node;
import ytex.kernel.tree.NodeMappingInfo;

public class CorpusKernelEvaluatorImpl implements CorpusKernelEvaluator {
	private Kernel instanceKernel;
	private InstanceTreeBuilder instanceTreeBuilder;
	private String trainInstanceIDQuery;
	private String testInstanceIDQuery;
	private List<NodeMappingInfo> nodeTypes;
	private String instanceIDField;
	private String instanceTreeQuery;
	private Map<String, Object> instanceTreeQueryArgs;
	private KernelEvaluationDao kernelEvaluationDao;
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private String kernelName;
	private PlatformTransactionManager transactionManager;
	private TransactionTemplate txTemplate;
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public Kernel getInstanceKernel() {
		return instanceKernel;
	}

	public void setInstanceKernel(Kernel instanceKernel) {
		this.instanceKernel = instanceKernel;
	}

	public InstanceTreeBuilder getInstanceTreeBuilder() {
		return instanceTreeBuilder;
	}

	public void setInstanceTreeBuilder(InstanceTreeBuilder instanceTreeBuilder) {
		this.instanceTreeBuilder = instanceTreeBuilder;
	}

	public String getTrainInstanceIDQuery() {
		return trainInstanceIDQuery;
	}

	public void setTrainInstanceIDQuery(String trainInstanceIDQuery) {
		this.trainInstanceIDQuery = trainInstanceIDQuery;
	}

	public String getTestInstanceIDQuery() {
		return testInstanceIDQuery;
	}

	public void setTestInstanceIDQuery(String testInstanceIDQuery) {
		this.testInstanceIDQuery = testInstanceIDQuery;
	}

	public List<NodeMappingInfo> getNodeTypes() {
		return nodeTypes;
	}

	public void setNodeTypes(List<NodeMappingInfo> nodeTypes) {
		this.nodeTypes = nodeTypes;
	}

	public String getInstanceIDField() {
		return instanceIDField;
	}

	public void setInstanceIDField(String instanceIDField) {
		this.instanceIDField = instanceIDField;
	}

	public String getInstanceTreeQuery() {
		return instanceTreeQuery;
	}

	public void setInstanceTreeQuery(String instanceTreeQuery) {
		this.instanceTreeQuery = instanceTreeQuery;
	}

	public Map<String, Object> getInstanceTreeQueryArgs() {
		return instanceTreeQueryArgs;
	}

	public void setInstanceTreeQueryArgs(
			Map<String, Object> instanceTreeQueryArgs) {
		this.instanceTreeQueryArgs = instanceTreeQueryArgs;
	}

	public KernelEvaluationDao getKernelEvaluationDao() {
		return kernelEvaluationDao;
	}

	public void setKernelEvaluationDao(KernelEvaluationDao kernelEvaluationDao) {
		this.kernelEvaluationDao = kernelEvaluationDao;
	}

	public String getKernelName() {
		return kernelName;
	}

	public void setKernelName(String kernelName) {
		this.kernelName = kernelName;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		txTemplate = new TransactionTemplate(this.transactionManager);
		txTemplate
				.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
	}

	protected class InstanceIDRowMapper implements RowMapper<Integer> {

		@Override
		public Integer mapRow(ResultSet rs, int arg1) throws SQLException {
			return rs.getInt(1);
		}

	}

	public void evaluateKernelOnCorpus() {
		List<Integer> documentIds = txTemplate
				.execute(new TransactionCallback<List<Integer>>() {
					@Override
					public List<Integer> doInTransaction(TransactionStatus arg0) {
						return simpleJdbcTemplate.query(trainInstanceIDQuery,
								new InstanceIDRowMapper());
					}
				});

		List<Integer> testDocumentIds = new ArrayList<Integer>();
		if (testInstanceIDQuery != null) {
			testDocumentIds = txTemplate
					.execute(new TransactionCallback<List<Integer>>() {
						@Override
						public List<Integer> doInTransaction(
								TransactionStatus arg0) {
							return simpleJdbcTemplate.query(
									testInstanceIDQuery,
									new InstanceIDRowMapper());
						}
					});
		}
		Set<String> names = new HashSet<String>(1);
		names.add(kernelName);
		final Map<Integer, Node> instanceIDMap = instanceTreeBuilder
				.loadInstanceTrees(nodeTypes, instanceIDField,
						instanceTreeQuery, instanceTreeQueryArgs);
		for (int i = 0; i < documentIds.size(); i++) {
			// left hand side of kernel evaluation
			int instanceId1 = documentIds.get(i);
			// list of instance ids right hand side of kernel evaluation
			SortedSet<Integer> rightDocumentIDs = new TreeSet<Integer>(
					testDocumentIds);
			if (i < (documentIds.size() - 1)) {
				rightDocumentIDs.addAll(documentIds.subList(i + 1,
						documentIds.size() - 1));
			}
			// remove instances already evaluated
			for (KernelEvaluation kEval : this.kernelEvaluationDao
					.getAllKernelEvaluationsForInstance(names, instanceId1)) {
				rightDocumentIDs
						.remove(instanceId1 == kEval.getInstanceId1() ? kEval
								.getInstanceId2() : kEval.getInstanceId1());
			}
			for (Integer instanceId2 : rightDocumentIDs) {
				if (instanceId1 != instanceId2) {
					final int i1 = instanceId1;
					final int i2 = instanceId2;
					final Node root1 = instanceIDMap.get(i1);
					final Node root2 = instanceIDMap.get(i2);
					if (root1 != null && root2 != null) {
						// store in separate tx so that there are less objects
						// in session for hibernate to deal with
						// txTemplate.execute(new TransactionCallback() {
						// @Override
						// public Object doInTransaction(TransactionStatus arg0)
						// {
						kernelEvaluationDao.storeKernel(kernelName, i1, i2,
								instanceKernel.evaluate(root1, root2));
					}
					// return null;
					// }
					// });
				}
			}
		}
	}

	public static void main(String args[]) {
		String beanRefContextBase = args.length > 0 ? args[0]
				: "kernelBeanRefContext.xml";
		String contextName = args.length > 1 ? args[1]
				: "kernelApplicationContext";
		String beanName = args.length > 2 ? args[1] : "corpusKernelEvaluator";
		String beanRefContext = "classpath*:" + beanRefContextBase;
		ApplicationContext cmcApplicationContext = (ApplicationContext) ContextSingletonBeanFactoryLocator
				.getInstance(beanRefContext).useBeanFactory(contextName)
				.getFactory();
		CorpusKernelEvaluator corpusEvaluator = cmcApplicationContext.getBean(
				beanName, CorpusKernelEvaluator.class);
		corpusEvaluator.evaluateKernelOnCorpus();
	}
}
