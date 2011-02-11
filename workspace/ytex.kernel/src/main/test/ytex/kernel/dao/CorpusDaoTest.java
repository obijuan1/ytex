package ytex.kernel.dao;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import ytex.kernel.KernelContextHolder;

public class CorpusDaoTest extends TestCase {
	CorpusDao corpusDao = null;

	@Override
	protected void setUp() throws Exception {
		corpusDao = (CorpusDao) KernelContextHolder.getApplicationContext()
				.getBean("corpusDao");
	}

	public void testUpdateCorpusTermFrequency() {
		Set<String> analysisBatches = new HashSet<String>(1);
		analysisBatches.add("cmc-ctakes");
		// corpusDao.updateCorpusTermFrequency("cmc-ctakes", analysisBatches);
	}

}
