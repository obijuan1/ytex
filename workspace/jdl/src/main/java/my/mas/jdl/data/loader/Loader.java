package my.mas.jdl.data.loader;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import my.mas.jdl.data.base.JdlConnection;

/**
 * Abstract Loader.
 * 
 * @author mas
 */
public abstract class Loader {
	/**
	 * @param preparedStatement
	 *            the PreparedStatement to execute
	 */
	public static void executeBatch(final PreparedStatement preparedStatement) {
		try {
			preparedStatement.addBatch();
			preparedStatement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//vng change - fail on error
			//			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param jdlConnection
	 *            the jdlConnection to manage
	 */
	public abstract void dataInsert(JdlConnection jdlConnection);
}
