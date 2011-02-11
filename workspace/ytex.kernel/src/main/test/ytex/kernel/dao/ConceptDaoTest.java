package ytex.kernel.dao;

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
		ConceptGraph cg = conceptDao.initializeConceptGraph(new String[] {});
		System.out.println("depth:" + cg.getDepthMax());
		System.out.println("concepts:" + cg.getConceptMap().size());
		System.out.println("roots:" + (cg.getRoots().size()));
		// System.out.println("roots:"+cg.getRoots());
		ConcRel crAscites = cg.getConceptMap().get("C0003962");
		System.out.println(crAscites);
		ConcRel crHepatoma = cg.getConceptMap().get("C2239176");
		System.out.println(crHepatoma);
		ObjPair<ConcRel, Integer> pairLCS = crAscites.getLeastCommonConcept(
				crAscites, crHepatoma);
		System.out.println(pairLCS);
	}

	public void testGetConceptGraph() {
		fail("Not yet implemented");
	}

}
