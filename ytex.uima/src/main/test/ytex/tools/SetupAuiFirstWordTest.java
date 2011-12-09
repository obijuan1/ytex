package ytex.tools;

import ytex.umls.model.UmlsAuiFirstWord;
import junit.framework.TestCase;

public class SetupAuiFirstWordTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

//	public void testSetupAuiFirstWord() throws Exception {
//		new SetupAuiFirstWord();
//	}
	
	public void testStem() throws Exception {
		SetupAuiFirstWord sa = new SetupAuiFirstWord();
		//UmlsAuiFirstWord fw = sa.tokenizeStr("A10773589", "Prostate-specific antigen");
//		UmlsAuiFirstWord fw = sa.tokenizeStr("A0231342", "t-butoxycarbonylleucyl-leucyl-leucyl-leucyl-aminoisobutyryl-leucyl-leucyl-leucyl-leucyl-aminoisobutyric");
		UmlsAuiFirstWord fw = sa.tokenizeStr("A0019656", "acyl-coa-1-acylglycero-3-phosphocholine-o-acyltransferase");
		System.out.println(fw);
		System.out.println(fw.getTokenizedStr());
		System.out.println(fw.getStemmedStr());
		fw = sa.tokenizeStr("test", "meds:lisinopril");
		System.out.println(fw);
//		A0019656	acyl-coa-1-acylglycero-3-phosphocholine-o-acyltransferase
//		A0138408	1-acetylnaphthylalanyl-2-para-fluorophenylalanyl-3-tryptophyl-6-arginine-lhrh
//		A0138430	1-alk-1'-enyl-2-acyl-sn-glycero-3-phosphorylethanolamine
//		A0138434	1-alkyl-2-acetyl-sn-glycero-3-phosphocholine-acetylhydrolase
//		A0138450	1-amino-1-benzyl-2-mercaptoethane-s-phenylcarbamate
//		A0138571	1-beta-arabinofuranosyl-5-butyluracil-5'-triphosphate
//		A0138573	1-beta-arabinofuranosyl-5-propyluracil-5'-triphosphate
	}

}
