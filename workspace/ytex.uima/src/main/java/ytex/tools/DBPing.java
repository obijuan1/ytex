package ytex.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/*
 * verify db parameters.  takes as input property file
 */
public class DBPing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InputStream is = null;
		try {
			is = new FileInputStream(args[0]);
			Properties props = new Properties();
			props.load(is);
			System.exit(ping(props));
		} catch (Exception e) {
			System.out
					.println("DBPing: Connection to db failed - please check your settings and try again");
			System.exit(1);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static int ping(Properties props) throws Exception {
		if (props.getProperty("db.driver") == null) {
			System.out.println("DBPing: db.driver not defined");
			return 1;
		}
		if (props.getProperty("db.url") == null) {
			System.out.println("DBPing: db.url not defined");
			return 1;
		}
		Class.forName(props.getProperty("db.driver"));
		Connection c = null;
		try {
			c = DriverManager.getConnection(props.getProperty("db.url"),
					props.getProperty("db.username"),
					props.getProperty("db.password"));
			System.out.println("DBPing: connection succeeded");
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (Exception e) {
				}
			}
		}
		return 0;
	}

}
