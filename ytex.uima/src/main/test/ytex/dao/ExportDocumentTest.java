package ytex.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import ytex.uima.ApplicationContextHolder;

public class ExportDocumentTest extends TestCase {
	DataSource ds;
	NamedParameterJdbcTemplate namedJdbcTemplate;

	protected void setUp() throws Exception {
		super.setUp();
		ds = (DataSource) ApplicationContextHolder.getApplicationContext()
				.getBean("vacsDataSource");
		namedJdbcTemplate = new NamedParameterJdbcTemplate(ds);
	}

	public void testExportDummyDocument() {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("studyid", 110132);
		params.put("uid", -19506);
		params.put("document_type_id", 1);
		namedJdbcTemplate
				.query(
						"select doc_text from esld.dummy_document where studyid = :studyid and uid = :uid and document_type_id = :document_type_id",
						params, new RowCallbackHandler() {
							@Override
							public void processRow(ResultSet rs)
									throws SQLException {
								LobHandler lobHandler = new DefaultLobHandler();
								String clobText = lobHandler.getClobAsString(
										rs, 1);
								BufferedWriter writer = null;
								try {
									writer = new BufferedWriter(new FileWriter(
											new File(params.get("uid")
													.toString()
													+ ".txt")));
									writer.write(clobText);
								} catch (IOException e) {
									e.printStackTrace();
								} finally {
									if (writer != null)
										try {
											writer.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
								}
							}
						});

	}

}
