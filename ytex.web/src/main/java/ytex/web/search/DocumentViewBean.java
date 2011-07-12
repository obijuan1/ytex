package ytex.web.search;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringEscapeUtils;

import ytex.dao.DocumentDao;
import ytex.model.Document;

/**
 * JSF bean for viewing a document retrieved via semanticSearch.jspx.
 * Relies on documentID parameter.
 * @author vijay
 *
 */
public class DocumentViewBean {
	
	private DocumentDao documentDao;
	
	private Document document;
	
	private String docText;

	public DocumentDao getDocumentDao() {
		return documentDao;
	}

	public void setDocumentDao(DocumentDao documentDao) {
		this.documentDao = documentDao;
	}
	
	public void loadDocument() {
		String strDocumentID = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("documentID");
		if(this.document == null || (strDocumentID != null && !this.document.getDocumentID().equals(Integer.parseInt(strDocumentID)))) {
			this.document = this.documentDao.getDocument(Integer.parseInt(strDocumentID));
			this.docText = StringEscapeUtils.escapeXml(document.getDocText()).replaceAll("\n", "<br>");
		}
	}

	public Document getDocument() {
		this.loadDocument();
		return document;
	}

	public String getDocText() {
		this.loadDocument();
		return docText;
	}

}
