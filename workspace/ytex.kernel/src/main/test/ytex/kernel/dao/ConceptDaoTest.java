package ytex.kernel.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.model.ObjPair;

public class ConceptDaoTest extends TestCase {
	ConceptDao conceptDao = null;

	@Override
	protected void setUp() throws Exception {
		conceptDao = (ConceptDao) KernelContextHolder.getApplicationContext()
				.getBean("conceptDao");
	}

	public void testInitializeConceptGraph() {
		ConceptGraph cg = conceptDao
				.createConceptGraph("snomed",
						"select cui1, cui2 from umls.MRREL where sab = 'SNOMEDCT' and rel = 'PAR'");
		System.out.println("depth:" + cg.getDepthMax());
		System.out.println("concepts:" + cg.getConceptMap().size());
		System.out.println("roots:" + (cg.getRoots().size()));
		// System.out.println("roots:"+cg.getRoots());
		// ascites - hepatoma
		testPath(cg, "C0003962", "C2239176");
		// ascites - varices
		testPath(cg, "C0003962", "C0042345");
	}

	private void testPath(ConceptGraph cg, String cui1, String cui2) {
		ConcRel crAscites = cg.getConceptMap().get(cui1);
		System.out.println(crAscites);
		ConcRel crHepatoma = cg.getConceptMap().get(cui2);
		System.out.println(crHepatoma);
		Set<ConcRel> lcses = new HashSet<ConcRel>();
		Map<ConcRel, List<List<ConcRel>>> paths = new HashMap<ConcRel, List<List<ConcRel>>>();
		int depth = ConcRel.getLeastCommonConcept(
				crAscites, crHepatoma, lcses, paths);
		System.out.println(paths);
		System.out.println(depth);
	}

}
