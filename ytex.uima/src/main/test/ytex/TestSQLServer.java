package ytex;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

public class TestSQLServer extends TestCase {
	public void testConnect() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:sqlserver://VHACONSQLR;databaseName=VACS_PROGNOTES;integratedSecurity=true");
		conn.close();
	}

}
