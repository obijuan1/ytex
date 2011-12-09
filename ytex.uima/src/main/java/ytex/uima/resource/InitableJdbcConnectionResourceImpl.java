package ytex.uima.resource;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.hibernate.JDBCException;

import ytex.uima.ApplicationContextHolder;

import edu.mayo.bmi.uima.core.resource.JdbcConnectionResourceImpl;
import gov.va.maveric.uima.util.WrappedConnection;

/**
 * copied from mayo JdbcConnectionResourceImpl.
 * extended to support connection initialization statements.
 * this is required for case-insensitive searches in oracle.
 * <p/>
 * modified to default to settings in ytex.properties in case
 * config parameters not specified in descriptor
 * 
 * @author vijay
 *
 */
public class InitableJdbcConnectionResourceImpl extends
		JdbcConnectionResourceImpl {
	private static final Log iv_logger = LogFactory
			.getLog(InitableJdbcConnectionResourceImpl.class);
	private Connection iv_conn;

	@Override
	public Connection getConnection() {
		return iv_conn;
	}

	@Override
	public void load(DataResource dr) throws ResourceInitializationException {
		ConfigurationParameterSettings cps = dr.getMetaData()
				.getConfigurationParameterSettings();
		Properties ytexProperties = ApplicationContextHolder.getYtexProperties();

		String driverClassName = (String) cps
				.getParameterValue(PARAM_DRIVER_CLASS);
		if(driverClassName == null)
			driverClassName = ytexProperties.getProperty("db.driver");
		
		String urlStr = (String) cps.getParameterValue(PARAM_URL);
		if(urlStr == null)
			urlStr = ytexProperties.getProperty("db.url");

		String username = (String) cps.getParameterValue(PARAM_USERNAME);
		if(username == null)
			username = ytexProperties.getProperty("db.username");

		String password = (String) cps.getParameterValue(PARAM_PASSWORD);
		if(password == null)
			password = ytexProperties.getProperty("db.password");
		

		Boolean keepAlive = new Boolean((String) cps
				.getParameterValue(PARAM_KEEP_ALIVE));

		String isolationStr = (String) cps.getParameterValue(PARAM_ISOLATION);

		String initStatements = (String) cps.getParameterValue("InitStatements");
		if(initStatements == null)
			initStatements = ytexProperties.getProperty("db.initStatements");

		try {
			if (keepAlive.booleanValue()) {
				iv_logger.info("Instantiating wrapped connection.");
				iv_conn = (Connection) new WrappedConnection(username,
						password, driverClassName, urlStr);
			} else {
				Class.forName(driverClassName);
				iv_conn = DriverManager.getConnection(urlStr, username,
						password);
			}

			if (initStatements != null && !initStatements.isEmpty()) {
				Statement stmt = null;
				try {
					stmt = iv_conn.createStatement();
					for (String statement : initStatements.split(";")) {
						iv_logger.info("executing init statement: " + statement);
						stmt.execute(statement);
					}
				} finally {
					if (stmt != null) {
						try {
							stmt.close();
						} catch (JDBCException ex) {
						}
					}
				}
			}

			iv_logger.info("Connection established to: " + urlStr);

			if (isolationStr != null) {
				// use java reflection to obtain the corresponding level integer
				Field f = Connection.class.getField(isolationStr);
				int level = f.getInt(null);
				iv_logger.info("Connection transaction isolation level set: "
						+ isolationStr + "(" + level + ")");
				iv_conn.setTransactionIsolation(level);
			}
		} catch (Exception e) {
			iv_logger.error("initialization error", e);
			throw new ResourceInitializationException(e);
		}
	}
}
