import junit.framework.TestCase;
import ytex.kernel.ConceptSimilarityService;
import ytex.kernel.KernelContextHolder;

public class ConceptSimilarityServiceTest extends TestCase {
	ConceptSimilarityService conceptSimilarityService;

	protected void setUp() throws Exception {
		super.setUp();
		conceptSimilarityService = (ConceptSimilarityService) KernelContextHolder
				.getApplicationContext().getBean("conceptSimilarityService");
	}

	public void testUpdateInformationContent() {
		conceptSimilarityService.updateInformationContent("cmc-ctakes");
	}

	public void testLin() {
		System.out.println(conceptSimilarityService.lin("cmc-ctakes",
				"C2239176", "C0003962"));

	}

	public void testLch() {
		System.out
				.println(conceptSimilarityService.lch("C2239176", "C0003962"));
	}

	public void testSim() {
		String pairs[][] = new String[][] {
				new String[] { "C0477176", "C2136108" },
				new String[] { "C2136112", "C2168238" },
				new String[] { "C2136110", "C2168238" },
				new String[] { "C1446337", "C2168238" },
				new String[] { "C2136108", "C2168238" } };
		for (String[] pair : pairs) {
			double lch = conceptSimilarityService.lch(pair[0], pair[1]);
			double lin = conceptSimilarityService.lin("cmc-ctakes", pair[0],
					pair[1]);
			Object lcs[] = conceptSimilarityService.lcs(pair[0], pair[1]);
			System.out.println(pair[0] + "\t" + pair[1] + "\t" + lch + "\t"
					+ lin + "\t" + lcs[0] + "\t" + lcs[1]);
		}
	}
}
