package ytex.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import ytex.uima.ApplicationContextHolder;

/**
 * read specified document id's cas, send to stdout
 * @author vijay
 *
 */
public class ExportCAS {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String documentID = args[0];
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		BufferedReader gzIS = null;
		Properties jdbcProperties = ApplicationContextHolder.getYtexProperties();

		try {
			Class.forName(jdbcProperties.getProperty("db.driver"));
			conn = DriverManager.getConnection(jdbcProperties
					.getProperty("db.url"), jdbcProperties
					.containsKey("db.username") ? jdbcProperties
					.getProperty("db.username") : null, jdbcProperties
					.containsKey("db.password") ? jdbcProperties
					.getProperty("db.password") : null);
			String strSQL = jdbcProperties.containsKey("db.schema") ? "select cas from "
					+ jdbcProperties.getProperty("db.schema")
					+ ".document where document_id = ?"
					: "select cas from document where document_id = ?";
			ps = conn.prepareStatement(strSQL);
			ps.setInt(1, Integer.parseInt(documentID));
			rs = ps.executeQuery();
			if (rs.next()) {
				gzIS = new BufferedReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(rs
						.getBinaryStream(1)))));
				String line;
				while((line = gzIS.readLine()) != null) {
					System.out.println(line);
				}
			} else {
				throw new RuntimeException("No document with id = "
						+ documentID);
			}
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
			}
			try {
				if (gzIS != null)
					gzIS.close();
			} catch (IOException e) {
			}
		}
	}

}
