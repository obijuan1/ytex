package ytex.uima;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;


public class WriteXMI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataSource ds = (DataSource) ApplicationContextHolder
				.getApplicationContext().getBean("vacsDataSource");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("analysis_batch", args[1]);
		final File directory = new File(args[0]);
		directory.mkdir();
		jdbcTemplate
				.query(
						"select document_id, cas from esld.document where analysis_batch = ?",
						new Object[] { args[1] }, new RowCallbackHandler() {
							@Override
							public void processRow(ResultSet rs)
									throws SQLException {
								LobHandler lobHandler = new DefaultLobHandler();
								int docId = rs.getInt(1);
								String clobText = lobHandler.getClobAsString(
										rs, 2);
								if (clobText == null) {
									System.out.println("warning, cas is null for docid="+docId);
								} else {
									FileWriter writer = null;
									try {
										writer = new FileWriter(new File(
												directory.getAbsolutePath()
														+ File.separator
														+ docId + ".xmi"));
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
							}
						});
	}
}
