package ytex.kernel;

import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;

public class ConceptGraphStats {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConceptDao cd = KernelContextHolder.getApplicationContext().getBean(
				ConceptDao.class);
		for (String cg : args) {
			printConceptGraphStats(cg, cd);
		}
	}

	private static void printConceptGraphStats(String cgName, ConceptDao cd) {
		ConceptGraph cg = cd.getConceptGraph(cgName);
		System.out.println(cgName + " concepts: " + cg.getConceptList().size());
		int relations = 0;
		for (ConcRel cr : cg.getConceptList()) {
			relations += cr.getChildren().size();
		}
		System.out.println(cgName + " relations: " + relations);
	}

}
