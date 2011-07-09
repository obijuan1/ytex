package ytex.umls.dao;

import java.util.List;
import java.util.Map;

import ytex.umls.model.UmlsAuiFirstWord;

public interface UMLSDao {

//	public abstract List<Object[]> getRelationsForSABs(String sabs[]);
//
//	public abstract List<Object[]> getAllRelations();

	/**
	 * get all aui, str from mrconso
	 */
	public List<Object[]>  getAllAuiStr(String lastAui);

	public void deleteAuiFirstWord();

	public void insertAuiFirstWord(List<UmlsAuiFirstWord> listAuiFirstWord);

	public abstract Map<String, String> getNames(List<String> subList);

}