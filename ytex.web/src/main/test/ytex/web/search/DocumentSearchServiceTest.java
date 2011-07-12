package ytex.web.search;

//import ytex.uima.ApplicationContextHolder;
import ytex.web.search.DocumentSearchService;
import junit.framework.TestCase;

public class DocumentSearchServiceTest extends TestCase {
	DocumentSearchService documentSearchDao;

	protected void setUp() throws Exception {
		super.setUp();
//		documentSearchDao = (DocumentSearchDao) ApplicationContextHolder
//				.getApplicationContext().getBean("documentSearchDao");
	}
	
	public void testSearch() {
		System.out.println(documentSearchDao.extendedSearch("C0003962", null, null, null, null, Boolean.TRUE));
	}

}
