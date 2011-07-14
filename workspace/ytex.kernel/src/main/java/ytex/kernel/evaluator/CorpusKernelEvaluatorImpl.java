package ytex.kernel.evaluator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.DBUtil;
import ytex.kernel.dao.KernelEvaluationDao;
import ytex.kernel.model.KernelEvaluation;
import ytex.kernel.model.KernelEvaluationInstance;
import ytex.kernel.tree.InstanceTreeBuilder;
import ytex.kernel.tree.Node;
import ytex.kernel.tree.TreeMappingInfo;

public class CorpusKernelEvaluatorImpl implements CorpusKernelEvaluator {
	private static final Log log = LogFactory
			.getLog(CorpusKernelEvaluator.class);

	protected class InstanceIDRowMapper implements RowMapper<Integer> {

		@Override
		public Integer mapRow(ResultSet rs, int arg1) throws SQLException {
			return rs.getInt(1);
		}

	}

	@SuppressWarnings("static-access")
	private static Options initOptions() {
		Option oBeanref = OptionBuilder
				.withArgName("classpath*:simSvcBeanRefContext.xml")
				.hasArg()
				.withDescription(
						"use specified beanRefContext.xml, default classpath*:simSvcBeanRefContext.xml")
				.create("beanref");
		Option oAppctx = OptionBuilder
				.withArgName("kernelApplicationContext")
				.hasArg()
				.withDescription(
						"use specified applicationContext, default kernelApplicationContext")
				.create("appctx");
		Option oBeans = OptionBuilder
				.withArgName("beans-corpus.xml")
				.hasArg()
				.withDescription(
						"use specified beans.xml, no default.  This file is typically required.")
				.create("beans");
		Option oHelp = new Option("help", "print this message");
		Options options = new Options();
		options.addOption(oBeanref);
		options.addOption(oAppctx);
		options.addOption(oBeans);
		options.addOption(oHelp);
		return options;
	}

	public static void main(String args[]) throws Exception {
		Options options = initOptions();

		if (args.length == 0) {
			printHelp(options);
		} else {
			CommandLineParser parser = new GnuParser();
			try {
				// parse the command line arguments
				CommandLine line = parser.parse(options, args);
				// parse the command line arguments
				String beanRefContext = line.getOptionValue("beanref",
						"classpath*:simSvcBeanRefContext.xml");
				String contextName = line.getOptionValue("appctx",
						"kernelApplicationContext");
				String beans = line.getOptionValue("beans");
				ApplicationContext appCtx = (ApplicationContext) ContextSingletonBeanFactoryLocator
						.getInstance(beanRefContext)
						.useBeanFactory(contextName).getFactory();
				ApplicationContext appCtxSource = appCtx;
				if (beans != null) {
					appCtxSource = new FileSystemXmlApplicationContext(
							new String[] { beans }, appCtx);
				}
				CorpusKernelEvaluator corpusEvaluator = appCtxSource.getBean(
						"corpusKernelEvaluator", CorpusKernelEvaluator.class);
				corpusEvaluator.evaluateKernelOnCorpus();
			} catch (ParseException e) {
				printHelp(options);
				throw e;
			}
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java ytex.kernel.evaluator.CorpusKernelEvaluatorImpl",
						options);
	}

	private DataSource dataSource;
	private Kernel instanceKernel;

	private InstanceTreeBuilder instanceTreeBuilder;

	private KernelEvaluationDao kernelEvaluationDao;

	public String getExperiment() {
		return experiment;
	}

	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getFoldId() {
		return foldId;
	}

	public void setFoldId(int foldId) {
		this.foldId = foldId;
	}

	private String experiment;
	private String name;
	private String label = DBUtil.getEmptyString();
	private double param1 = 0;
	private String param2 = DBUtil.getEmptyString();
	private int foldId = 0;

	public double getParam1() {
		return param1;
	}

	public void setParam1(double param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	private SimpleJdbcTemplate simpleJdbcTemplate;
	private String testInstanceIDQuery;
	private String trainInstanceIDQuery;
	private PlatformTransactionManager transactionManager;

	private TreeMappingInfo treeMappingInfo;

	private TransactionTemplate txTemplate;

	public void evaluateKernelOnCorpus(final Map<Integer, Node> instanceIDMap,
			int nMod, int nSlice) {
		KernelEvaluation kernelEvaluationTmp = new KernelEvaluation();
		kernelEvaluationTmp.setExperiment(this.getExperiment());
		kernelEvaluationTmp.setFoldId(this.getFoldId());
		kernelEvaluationTmp.setLabel(this.getLabel());
		kernelEvaluationTmp.setCorpusName(this.getName());
		kernelEvaluationTmp.setParam1(getParam1());
		kernelEvaluationTmp.setParam2(getParam2());
		final KernelEvaluation kernelEvaluation = this.kernelEvaluationDao
				.storeKernelEval(kernelEvaluationTmp);
		final List<Integer> documentIds = txTemplate
				.execute(new TransactionCallback<List<Integer>>() {
					@Override
					public List<Integer> doInTransaction(TransactionStatus arg0) {
						return simpleJdbcTemplate.query(trainInstanceIDQuery,
								new InstanceIDRowMapper());
					}
				});

		final List<Integer> testDocumentIds = new ArrayList<Integer>();
		if (testInstanceIDQuery != null) {
			testDocumentIds.addAll(txTemplate
					.execute(new TransactionCallback<List<Integer>>() {
						@Override
						public List<Integer> doInTransaction(
								TransactionStatus arg0) {
							return simpleJdbcTemplate.query(
									testInstanceIDQuery,
									new InstanceIDRowMapper());
						}
					}));
		}
		int nStart = 0;
		int nEnd = documentIds.size();
		int total = 0;
		documentIds.size();
		if (nMod > 0) {
			nMod = Math.min(total, nMod);
		}
		if (nMod > 0 && nSlice > nMod) {
			log.info("more slices than documents, skipping slice: " + nSlice);
			return;
		}
		if (nMod > 0) {
			int sliceSize = total / nMod;
			nStart = sliceSize * (nSlice - 1);
			if (nSlice != nMod)
				nEnd = nStart + sliceSize;
		}
		for (int i = nStart; i < nEnd; i++) {
			// left hand side of kernel evaluation
			final int instanceId1 = documentIds.get(i);
			if (log.isInfoEnabled())
				log.info("evaluating kernel for instance_id1 = " + instanceId1);
			// list of instance ids right hand side of kernel evaluation
			final SortedSet<Integer> rightDocumentIDs = new TreeSet<Integer>(
					testDocumentIds);
			if (i < documentIds.size()) {
				// rightDocumentIDs.addAll(documentIds.subList(i + 1,
				// documentIds.size() - 1));
				rightDocumentIDs.addAll(documentIds.subList(i,
						documentIds.size()));
			}
			// remove instances already evaluated
			for (KernelEvaluationInstance kEval : this.kernelEvaluationDao
					.getAllKernelEvaluationsForInstance(kernelEvaluation,
							instanceId1)) {
				rightDocumentIDs
						.remove(instanceId1 == kEval.getInstanceId1() ? kEval
								.getInstanceId2() : kEval.getInstanceId1());
			}
			// kernel evaluations for this instance are done in a single tx
			// hibernate can batch insert these
			txTemplate.execute(new TransactionCallback<Object>() {

				@Override
				public Object doInTransaction(TransactionStatus arg0) {
					evalInstance(instanceIDMap, kernelEvaluation, instanceId1,
							rightDocumentIDs);
					return null;
				}
			});

		}
	}

	private void evalInstance(Map<Integer, Node> instanceIDMap,
			KernelEvaluation kernelEvaluation, int instanceId1,
			SortedSet<Integer> rightDocumentIDs) {
		for (Integer instanceId2 : rightDocumentIDs) {
			// if (instanceId1 != instanceId2) {
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
				kernelEvaluationDao.storeKernel(kernelEvaluation, i1, i2,
						instanceKernel.evaluate(root1, root2));
			}
			// return null;
			// }
			// });
			// }
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public Kernel getInstanceKernel() {
		return instanceKernel;
	}

	public InstanceTreeBuilder getInstanceTreeBuilder() {
		return instanceTreeBuilder;
	}

	public KernelEvaluationDao getKernelEvaluationDao() {
		return kernelEvaluationDao;
	}

	public String getTestInstanceIDQuery() {
		return testInstanceIDQuery;
	}

	public String getTrainInstanceIDQuery() {
		return trainInstanceIDQuery;
	}

	//
	// public List<NodeMappingInfo> getNodeTypes() {
	// return nodeTypes;
	// }
	//
	// public void setNodeTypes(List<NodeMappingInfo> nodeTypes) {
	// this.nodeTypes = nodeTypes;
	// }
	//
	// public String getInstanceIDField() {
	// return instanceIDField;
	// }
	//
	// public void setInstanceIDField(String instanceIDField) {
	// this.instanceIDField = instanceIDField;
	// }
	//
	// public String getInstanceTreeQuery() {
	// return instanceTreeQuery;
	// }
	//
	// public void setInstanceTreeQuery(String instanceTreeQuery) {
	// this.instanceTreeQuery = instanceTreeQuery;
	// }
	//
	// public Map<String, Object> getInstanceTreeQueryArgs() {
	// return instanceTreeQueryArgs;
	// }
	//
	// public void setInstanceTreeQueryArgs(
	// Map<String, Object> instanceTreeQueryArgs) {
	// this.instanceTreeQueryArgs = instanceTreeQueryArgs;
	// }

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public TreeMappingInfo getTreeMappingInfo() {
		return treeMappingInfo;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public void setInstanceKernel(Kernel instanceKernel) {
		this.instanceKernel = instanceKernel;
	}

	public void setInstanceTreeBuilder(InstanceTreeBuilder instanceTreeBuilder) {
		this.instanceTreeBuilder = instanceTreeBuilder;
	}

	public void setKernelEvaluationDao(KernelEvaluationDao kernelEvaluationDao) {
		this.kernelEvaluationDao = kernelEvaluationDao;
	}

	public void setTestInstanceIDQuery(String testInstanceIDQuery) {
		this.testInstanceIDQuery = testInstanceIDQuery;
	}

	public void setTrainInstanceIDQuery(String trainInstanceIDQuery) {
		this.trainInstanceIDQuery = trainInstanceIDQuery;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		txTemplate = new TransactionTemplate(this.transactionManager);
		txTemplate
				.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
	}

	public void setTreeMappingInfo(TreeMappingInfo treeMappingInfo) {
		this.treeMappingInfo = treeMappingInfo;
	}

	@Override
	public void evaluateKernelOnCorpus() {
		final Map<Integer, Node> instanceIDMap = instanceTreeBuilder
				.loadInstanceTrees(treeMappingInfo);
		this.evaluateKernelOnCorpus(instanceIDMap, 0, 0);
	}

	public class SliceEvaluator implements Callable<Object> {
		int nSlice;
		Map<Integer, Node> instanceIDMap;
		int nMod;

		public SliceEvaluator(Map<Integer, Node> instanceIDMap, int nMod,
				int nSlice) {
			this.nSlice = nSlice;
			this.nMod = nMod;
			this.instanceIDMap = instanceIDMap;
		}

		@Override
		public Object call() throws Exception {
			try {
				evaluateKernelOnCorpus(instanceIDMap, nMod, nSlice);
			} catch (Exception e) {
				log.error("error on slice: " + nSlice, e);
				throw e;
			}
			return null;
		}
	}

	@Override
	public void evaluateKernelOnCorpus(Map<Integer, Node> instanceIDMap,
			int nMod) throws InterruptedException {
		ExecutorService svc = Executors.newFixedThreadPool(nMod);
		List<Callable<Object>> taskList = new ArrayList<Callable<Object>>(nMod);
		for (int nSlice = 1; nSlice <= nMod; nSlice++) {
			taskList.add(new SliceEvaluator(instanceIDMap, nMod, nSlice));
		}
		svc.invokeAll(taskList);
		svc.shutdown();
		svc.awaitTermination(60 * 4, TimeUnit.MINUTES);
	}
}
