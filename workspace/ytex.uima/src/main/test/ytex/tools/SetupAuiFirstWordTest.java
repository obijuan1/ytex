package ytex.tools;

import ytex.umls.model.UmlsAuiFirstWord;
import junit.framework.TestCase;

public class SetupAuiFirstWordTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSetupAuiFirstWord() throws Exception {
		new SetupAuiFirstWord();
	}
	
	public void testStem() throws Exception {
		SetupAuiFirstWord sa = new SetupAuiFirstWord();
		UmlsAuiFirstWord fw = sa.tokenizeStr("A10773589", "Prostate-specific antigen");
		System.out.println(fw.getTokenizedStr());
		
	}

}
