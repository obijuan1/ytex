package ytex.cmc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.dao.KernelEvaluationDao;
import ytex.kernel.model.KernelEvaluation;

public class CMCEvaluatorImpl implements CMCEvaluator {
	SessionFactory sessionFactory;
	KernelEvaluationDao kernelEvaluationDao;
	CMCKernel kernel;
	PlatformTransactionManager transactionManager;
	TransactionTemplate txTemplate;
	String name;

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

	public KernelEvaluationDao getKernelEvaluationDao() {
		return kernelEvaluationDao;
	}

	public void setKernelEvaluationDao(KernelEvaluationDao kernelEvaluationDao) {
		this.kernelEvaluationDao = kernelEvaluationDao;
	}

	public CMCKernel getKernel() {
		return kernel;
	}

	public void setKernel(CMCKernel kernel) {
		this.kernel = kernel;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.cmc.CMCEvaluator#evaluateAllCMC()
	 */
	@SuppressWarnings("unchecked")
	public void evaluateAllCMC() {
		Query q = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select documentId from CMCDocument where documentSet = 'train' order by documentId asc");
		// q.setMaxResults(100);
		List<Integer> documentIds = q.list();
		Query qtest = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select documentId from CMCDocument where documentSet = 'test' order by documentId asc");
		Set<String> names = new HashSet<String>(1);
		names.add(name);
		List<Integer> testDocumentIds = qtest.list();
		// Map<KernelEvalKey, Double> mapKernelEval = kernelEvaluationDao
		// .getAllKernelEvaluations("cmc-ctakes");
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
					// if (!mapKernelEval.containsKey(new KernelEvalKey(i1,
					// i2))) {
					// store in separate tx so that there are less objects
					// in
					// session for hibernate to deal with
					txTemplate.execute(new TransactionCallback() {
						@Override
						public Object doInTransaction(TransactionStatus arg0) {
							kernelEvaluationDao.storeKernel(name, i1,
									i2, kernel.calculateSimilarity(i1, i2));
							return null;
						}
					});
					// }
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
