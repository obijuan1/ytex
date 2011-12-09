package ytex.web.search;

import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;


/**
 * Jsf bean for full text search.
 * 
 * 
 * @author vijay
 *
 */
public class FullTextSearchBean {
	private String searchTerm;
	private List<Map<String,Object>> searchResultList;
	private DocumentSearchService documentSearchService;
	
	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public List<Map<String, Object>> getSearchResultList() {
		return searchResultList;
	}

	public void setSearchResultList(List<Map<String, Object>> searchResultList) {
		this.searchResultList = searchResultList;
	}

	public DocumentSearchService getDocumentSearchService() {
		return documentSearchService;
	}

	public void setDocumentSearchService(DocumentSearchService documentSearchService) {
		this.documentSearchService = documentSearchService;
	}

	public void searchListen(ActionEvent event) {
		if(searchTerm != null && searchTerm.trim().length() > 0)
			this.searchResultList = documentSearchService.fullTextSearch(searchTerm);
	}
}
