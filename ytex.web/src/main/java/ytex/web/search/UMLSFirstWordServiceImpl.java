package ytex.web.search;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class UMLSFirstWordServiceImpl implements UMLSFirstWordService,
		InitializingBean {
	public static class UMLSFirstWordRowMapper implements
			RowMapper<UMLSFirstWord> {

		public UMLSFirstWord mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			UMLSFirstWord fword = new UMLSFirstWord();
			fword.setCui(rs.getString("cui"));
			fword.setFword(rs.getString("fword"));
			fword.setText(rs.getString("text"));
			return fword;
		}

	}

	private DataSource dataSource;
	private SimpleJdbcTemplate jdbcTemplate;
	private String query;

	private Properties searchProperties;

	private Properties ytexProperties;

	public void afterPropertiesSet() throws Exception {
		String dbName = this.getYtexProperties().getProperty("db.name");
		String dbSchema = this.getYtexProperties().getProperty("db.schema");
		String umlsSchema = this.getYtexProperties().getProperty("umls.schema",
				dbSchema);
		String umlsCatalog = this.getYtexProperties().getProperty(
				"umls.catalog", dbName);
		this.query = searchProperties.getProperty("retrieveCUIByFword")
				.replaceAll("@db\\.schema@",
						this.getYtexProperties().getProperty("db.schema"));
		this.query = this.query.replaceAll("@umls\\.schema@", umlsSchema);
		this.query = this.query.replaceAll("@umls\\.catalog@", umlsCatalog);
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public Properties getSearchProperties() {
		return searchProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.va.vacs.esld.dao.UMLSFirstWordDao#getUMLSbyFirstWord(java.lang.String
	 * )
	 */
	public List<UMLSFirstWord> getUMLSbyFirstWord(String textStart) {
		String words[] = textStart.toLowerCase().split("\\s+");
		String fword = textStart.toLowerCase();
		// int nFWordLength = fword.length();
		String text = textStart.toLowerCase();
		int nTextLength = textStart.length();
		if (words.length > 1) {
			fword = words[0];
			// nFWordLength = fword.length();
		}
		// return this.jdbcTemplate.query(query, new UMLSFirstWordRowMapper(),
		// new Object[] { fword.length(), fword, nTextLength, text });
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("fword", fword);
		args.put("fwordlen", fword.length());
		args.put("term", text);
		args.put("termlen", nTextLength);
		return this.jdbcTemplate.query(query, new UMLSFirstWordRowMapper(),
				args);
	}

	public Properties getYtexProperties() {
		return ytexProperties;
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
