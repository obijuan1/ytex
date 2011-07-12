package ytex.kernel.evaluator;

import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConceptGraph;

/**
 * simple class to hold concept graph in the spring session. Kernels reference
 * this to get at concept graph.
 * 
 * @author vijay
 * 
 */
public class ConceptGraphHolderImpl implements ConceptGraphHolder {
	private ConceptGraph conceptGraph;
	private ConceptDao conceptDao;
	private String conceptGraphPath;

	public ConceptDao getConceptDao() {
		return conceptDao;
	}

	public void setConceptDao(ConceptDao conceptDao) {
		this.conceptDao = conceptDao;
	}

	/* (non-Javadoc)
	 * @see ytex.kernel.evaluator.ConceptGraphHolder#getConceptGraph()
	 */
	@Override
	public ConceptGraph getConceptGraph() {
		return conceptGraph;
	}

	public void setConceptGraph(ConceptGraph conceptGraph) {
		this.conceptGraph = conceptGraph;
	}

	public String getConceptGraphPath() {
		return conceptGraphPath;
	}

	public void setConceptGraphPath(String conceptGraphPath) {
		this.conceptGraphPath = conceptGraphPath;
	}

	public void init() {
//		this.conceptGraph = conceptDao.getConceptGraph(conceptGraphPath);
	}
}
