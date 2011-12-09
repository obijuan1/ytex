package ytex.web.search;

//import ytex.uima.ApplicationContextHolder;
import junit.framework.TestCase;

public class UMLSFirstWordServiceTest extends TestCase {
	UMLSFirstWordService umlsFirstWordDao;

	protected void setUp() throws Exception {
		super.setUp();
//		umlsFirstWordDao = (UMLSFirstWordDao)ApplicationContextHolder
//		.getApplicationContext().getBean("umlsFirstWordDao");
	}

	public void testGetUMLSbyFirstWord() {
		System.out.println(umlsFirstWordDao.getUMLSbyFirstWord("asc"));
	}

}
