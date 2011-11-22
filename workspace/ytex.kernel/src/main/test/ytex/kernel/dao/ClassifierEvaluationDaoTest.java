package ytex.kernel.dao;

import ytex.kernel.KernelContextHolder;
import junit.framework.TestCase;

public class ClassifierEvaluationDaoTest extends TestCase {
	ClassifierEvaluationDao classifierEvaluationDao;

	protected void setUp() throws Exception {
		classifierEvaluationDao = KernelContextHolder.getApplicationContext()
				.getBean(ClassifierEvaluationDao.class);
	}

	public void testGetTopFeatures() {
		System.out.println(this.classifierEvaluationDao.getTopFeatures(
				"i2b2.2008", null, "Asthma", "infogain-propagated", 0, 0,
				"rbpar", 25));
	}

	public void testGetThresholdFeatures() {
		System.out.println(this.classifierEvaluationDao.getThresholdFeatures(
				"i2b2.2008", "ctakes", "Hypertriglyceridemia",
				"infogain-propagated", 0, 0, "rbpar", 0.01));
	}

	public void testGetImputedFeaturesByPropagatedCutoff() {
		System.out.println(this.classifierEvaluationDao
				.getImputedFeaturesByPropagatedCutoff("i2b2.2008", "ctakes",
						"Hypertriglyceridemia", "infogain-propagated", "rbpar",
						"infogain-imputed", 10));
	}

}
