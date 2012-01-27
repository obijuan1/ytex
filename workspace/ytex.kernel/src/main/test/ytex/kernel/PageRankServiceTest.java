package ytex.kernel;

import java.util.HashMap;
import java.util.Map;

import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.pagerank.PageRankService;
import junit.framework.TestCase;

public class PageRankServiceTest extends TestCase {
	ConceptGraph cg;
	PageRankService pageRankService;

	protected void setUp() throws Exception {
		super.setUp();
		cg = new ConceptGraph();
		ConcRel a = cg.addConcept("a");
		ConcRel b = cg.addConcept("b");
		ConcRel c = cg.addConcept("c");
		ConcRel d = cg.addConcept("d");
		cg.getConceptMap().put(a.getConceptID(), a);
		cg.getConceptMap().put(b.getConceptID(), b);
		cg.getConceptMap().put(c.getConceptID(), c);
		cg.getConceptMap().put(d.getConceptID(), d);
		addRelationship(a, b);
		addRelationship(a, c);
		addRelationship(b, c);
		addRelationship(c, d);
		addRelationship(d, a);
		this.pageRankService = KernelContextHolder.getApplicationContext()
				.getBean(PageRankService.class);
	}

	protected void addRelationship(ConcRel source, ConcRel target) {
		target.parents.add(source);
		source.children.add(target);
	}

	public void testPageRank() {
		double[] pr = pageRankService.rank2(null, cg, 30, 1e-4, 0.85);
		printPR(pr);
		printPR(pageRankService.rank(null, cg, 30, 1e-4, 0.85));
	}

	private void printPR(double[] pr) {
		for (int i = 0; i < cg.getConceptList().size(); i++) {
			System.out.print(cg.getConceptList().get(i).getConceptID() + "="
					+ pr[i] + " ");
		}
		System.out.println();
	}
	
	private void printPR(Map<String, Double> pr) {
		for (ConcRel cr : cg.getConceptList()) {
			System.out.print(cr.getConceptID() + "="
					+ pr.get(cr.getConceptID()) + " ");
		}
		System.out.println();
	}

	public void testPersonalizedPageRank() {
		Map<Integer, Double> ppv2 = new HashMap<Integer, Double>();
		ppv2.put(cg.getConceptMap().get("d").getNodeIndex(), 1d);
		printPR(pageRankService.rank2(ppv2, cg, 30, 1e-2, 0.85));
		Map<String, Double> ppv = new HashMap<String, Double>();
		ppv.put("d", 1d);
		printPR(pageRankService.rank(ppv, cg, 30, 1e-4, 0.85));
	}

	public void testSim() {
		Map<Integer, Double> ppv2 = new HashMap<Integer, Double>();
		Map<String, Double> ppv = new HashMap<String, Double>();
		ppv.put("a", 1d);
		ppv2.put(cg.getConceptMap().get("a").getNodeIndex(), 1d);
		printPR(pageRankService.rank(ppv, cg, 30, 1e-4, 0.85));
		printPR(pageRankService.rank2(ppv2, cg, 30, 1e-4, 0.85));
		ppv = new HashMap<String, Double>();
		ppv.put("b", 1d);
		ppv2.put(cg.getConceptMap().get("b").getNodeIndex(), 1d);
		printPR(pageRankService.rank(ppv, cg, 30, 1e-4, 0.85));
		printPR(pageRankService.rank2(ppv2, cg, 30, 1e-4, 0.85));
		System.out.println(pageRankService.sim("a", "b", cg, 30, 1e-3, 0.85));
	}
}
