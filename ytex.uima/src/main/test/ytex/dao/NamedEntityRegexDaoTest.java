package ytex.dao;

import junit.framework.TestCase;
import ytex.model.NamedEntityRegex;
import ytex.uima.ApplicationContextHolder;

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
