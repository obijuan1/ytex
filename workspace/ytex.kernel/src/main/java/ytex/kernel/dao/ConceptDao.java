package ytex.kernel.dao;

import ytex.kernel.model.ConceptGraph;

public interface ConceptDao {

	public abstract ConceptGraph initializeConceptGraph(
			String[] sourceVocabularies);

	public abstract ConceptGraph getConceptGraph(String[] sourceVocabularies);

}