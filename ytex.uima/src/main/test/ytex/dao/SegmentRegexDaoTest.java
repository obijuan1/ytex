package ytex.dao;

import junit.framework.TestCase;
import ytex.model.SegmentRegex;
import ytex.uima.ApplicationContextHolder;

public class SegmentRegexDaoTest extends TestCase {
	SegmentRegexDao segmentRegexDao;

	protected void setUp() throws Exception {
		super.setUp();
		segmentRegexDao = (SegmentRegexDao) ApplicationContextHolder
				.getApplicationContext().getBean("segmentRegexDao");
	}
	
	public void testGetAllNeRegex() {
		for(SegmentRegex ne : segmentRegexDao.getSegmentRegexs()) {
			System.out.println(ne);
		}
	}

}
