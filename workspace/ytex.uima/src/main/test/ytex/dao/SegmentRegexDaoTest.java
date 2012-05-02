package ytex.dao;

import junit.framework.TestCase;
import ytex.uima.ApplicationContextHolder;
import ytex.uima.dao.SegmentRegexDao;
import ytex.uima.model.SegmentRegex;

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
