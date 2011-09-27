package ytex.web.search;


import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Service for searching documents.
 * Executes queries defined in ytex/search.properties.
 * @author vijay
 *
 */
public interface DocumentSearchService {

	public abstract List<DocumentSearchResult> searchByCui(String cui);

	/**
	 * Extended search
	 * @param code concept CUI or code.  this is the only required argument
	 * @param documentTypeName document type name.  (in VACS @see DocumentType)
	 * @param dateFrom document date greater than or equal to this
	 * @param dateTo document date less than or equal to this
	 * @param patientId patient id (study id in VACS)
	 * @param negationStatus true - only affirmed terms.  false - only negated terms. 
	 * @return list of results matching query
	 */
	public List<DocumentSearchResult> extendedSearch(String code, String documentTypeName, Date dateFrom,
			Date dateTo, Integer patientId, Boolean negationStatus);

	/**
	 * perform full text search
	 * @param searchTerm
	 * @return list of maps for each record.  map keys correspond to search query headings.  map (i.e. query) must contain DOCUMENT_ID (integer) and NOTE (string) fields.
	 */
	public List<Map<String, Object>> fullTextSearch(String searchTerm);

	/**
	 * retrieve note for specified document id, retrieved via full text search
	 * @param documentId
	 * @return note text.
	 */
	public String getFullTextSearchDocument(int documentId);

}