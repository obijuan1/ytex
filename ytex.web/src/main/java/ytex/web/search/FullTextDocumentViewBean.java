package ytex.web.search;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * Jsf bean to view results of fullTextSearch.jspx.
 * Relies on documentID parameter.
 * @author vijay
 *
 */
public class FullTextDocumentViewBean {
	private String docText;
	private DocumentSearchService documentSearchService;

	private int documentID;

	public int getDocumentID() {
		this.loadDocument();
		return documentID;
	}

	public String getDocText() {
		this.loadDocument();
		return docText;
	}
	public DocumentSearchService getDocumentSearchService() {
		return documentSearchService;
	}

	public void setDocumentSearchService(DocumentSearchService documentSearchService) {
		this.documentSearchService = documentSearchService;
	}

	public void loadDocument() {
		if (this.docText == null) {
			String strDocumentID = (String) FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap().get(
							"documentID");
			documentID = Integer.parseInt(strDocumentID); 
			String docTextUnformatted = this.documentSearchService
					.getFullTextSearchDocument(documentID);
			this.docText = StringEscapeUtils.escapeXml(docTextUnformatted).replaceAll("\n", "<br>");
		}
	}
}
