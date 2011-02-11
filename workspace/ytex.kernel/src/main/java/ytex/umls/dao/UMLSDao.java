package ytex.umls.dao;

import java.util.List;

public interface UMLSDao {

	public abstract List<Object[]> getRelationsForSABs(String sabs[]);

	public abstract List<Object[]> getAllRelations();

}