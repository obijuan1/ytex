package ytex.web.search;

import java.io.Serializable;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * JSF bean for viewing a document retrieved via semanticSearch.jspx. Relies on
 * documentID parameter.
 * 
 * @author vijay
 * 
 */
public class DocumentViewBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient DataSource dataSource;
	private String docText;
	private int documentID;
	private transient SimpleJdbcTemplate jdbcTemplate;
	private String rawText;

	private Properties searchProperties;

	private Properties ytexProperties;

	public DataSource getDataSource() {
		return dataSource;
	}

	public String getDocText() {
		this.loadDocument();
		return docText;
	}

	public int getDocumentID() {
		String strDocumentID = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("documentID");
		if (strDocumentID != null) {
			try {
				documentID = Integer.parseInt(strDocumentID);
			} catch (NumberFormatException nfe) {

			}
		}
		return documentID;
	}

	private String getQuery() {
		return searchProperties.getProperty("retrieveDocumentByID").replaceAll(
				"@db\\.schema@",
				this.getYtexProperties().getProperty("db.schema"));
	}

	public String getRawText() {
		return rawText;
	}

	public Properties getSearchProperties() {
		return searchProperties;
	}

	public Properties getYtexProperties() {
		return ytexProperties;
	}

	public void loadDocument() {
		if (getDocumentID() != 0) {
			this.rawText = loadRawText(documentID);
			if (rawText != null)
				this.docText = StringEscapeUtils.escapeXml(rawText).replaceAll(
						"\n", "<br>");
		}
	}

	private String loadRawText(int documentId) {
		return this.jdbcTemplate.queryForObject(this.getQuery(), String.class,
				documentId);
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public void setSearchProperties(Properties searchProperties) {
		this.searchProperties = searchProperties;
	}

	public void setYtexProperties(Properties ytexProperties) {
		this.ytexProperties = ytexProperties;
	}

}
