package ytex.dao;

import junit.framework.TestCase;
import ytex.uima.ApplicationContextHolder;
import ytex.uima.dao.NamedEntityRegexDao;
import ytex.uima.model.NamedEntityRegex;

public class NamedEntityRegexDaoTest extends TestCase {
	NamedEntityRegexDao neRegexDao;

	protected void setUp() throws Exception {
		super.setUp();
		neRegexDao = (NamedEntityRegexDao) ApplicationContextHolder
				.getApplicationContext().getBean("namedEntityRegexDao");
	}
	
	public void testGetAllNeRegex() {
		for(NamedEntityRegex ne : neRegexDao.getNamedEntityRegexs()) {
			System.out.println(ne);
		}
	}

}
