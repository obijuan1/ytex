package ytex.kernel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import ytex.kernel.model.ConceptGraph;

public class ConceptSimilarityServiceTest extends TestCase {
	private static final String conceptsToTest[][] = new String[][] {
			new String[] { "C0678181", "C0709711", "zocor", "zocor 5mg" },
			new String[] { "C0678181", "C0593906", "zocor", "lipitor" },

			new String[] { "C0699142", "C0678181", "tylenol", "zocor" },
			new String[] { "C0017245", "C0678181", "Gemfibrozil", "zocor" },
			new String[] { "C0017245", "C0723893", "Gemfibrozil", "Tricor" },
			new String[] { "C0008320", "C0041004", "Cholecystectomy",
					"Triglyceride" },
			new String[] { "C0008320", "C0740087", "Cholecystectomy",
					"Nasal oxygen catheter" },
			new String[] { "C0008320", "C0020699", "Cholecystectomy",
					"Hysterectomy" },
			new String[] { "C0008320", "C0003611", "Cholecystectomy",
					"Appendectomy" },
			new String[] { "C0008320", "C0162575", "Cholecystectomy",
					"Embolectomy" },
			new String[] { "C0008320", "C0027695", "Cholecystectomy",
					"Nephrectomy" },
			new String[] { "C0579044", "C0579042", "Bruise of tongue",
					"Bruise of palate" },
			new String[] { "C0579044", "C0542526", "Bruise of tongue",
					"Bruise of oral cavity" } };

	private ConceptSimilarityService conceptSimilarityService;

	// private SemanticTypeKernel semanticTypeKernel;

	protected void setUp() throws Exception {
		super.setUp();
		conceptSimilarityService = SimSvcContextHolder.getApplicationContext()
				.getBean(ConceptSimilarityService.class);
		// ApplicationContext appCtxSource = new
		// FileSystemXmlApplicationContext(
		// new String[] { "./i2b2.2008/libsvm/segrbkn/0.07/14/segrbkn.xml" },
		// SimSvcContextHolder.getApplicationContext());
		// semanticTypeKernel = appCtxSource.getBean(SemanticTypeKernel.class);
	}

	public void testSim() {
		System.out.println("cui\tcui\tlcs\tlcs depth\tlin\tlch");
		ConceptGraph cg = conceptSimilarityService.getConceptGraph();
		for (String[] line : conceptsToTest) {
			System.out.print(line[0]);
			System.out.print("\t");
			System.out.println(cg.getConceptMap().get(line[0]));
			System.out.print(line[1]);
			System.out.print("\t");
			System.out.println(cg.getConceptMap().get(line[1]));
		}

		for (String[] line : conceptsToTest) {
			Map<String, List<List<String>>> lcsPath = new HashMap<String, List<List<String>>>();
			String cui1 = line[0];
			String cui2 = line[1];
			System.out.print(cui1);
			System.out.print("\t");
			System.out.print(cui2);
			System.out.print("\t");
			System.out.print(line[2]);
			System.out.print("\t");
			System.out.print(line[3]);
			System.out.print("\t");
			int lcsDist = conceptSimilarityService.lcs(cui1, cui2, lcsPath);
			System.out.print(lcsDist);
			System.out.print("\t");
			System.out.print(lcsPath.keySet());
			System.out.print("\t");
			System.out.print(conceptSimilarityService.lin(cui1, cui2));
			System.out.print("\t");
			System.out.println(conceptSimilarityService.lch(cui1, cui2));
			System.out.print("\t");
			System.out.println(conceptSimilarityService.lch(cui1, cui2));
		}
	}
}
