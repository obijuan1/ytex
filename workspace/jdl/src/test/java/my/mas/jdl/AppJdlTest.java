package my.mas.jdl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import my.mas.jdl.common.FileUtil;
import my.mas.jdl.data.base.JdlConnection;
import my.mas.jdl.data.xml.jaxb.ObjectFactoryUtil;
import my.mas.jdl.schema.xdl.JdbcType;
import my.mas.jdl.test.PropFileMaps;
import my.mas.jdl.test.Resources;
import my.mas.jdl.test.SqlJdl;

import org.junit.BeforeClass;
import org.junit.Test;

public class AppJdlTest {
	private static JdbcType jdbc;
	private static JdlConnection jdlConnection;
	private static String CX = FileUtil.getFile(Resources.CONN_X).toString();
	private static String D2C = FileUtil.getFile(Resources.DATA2C).toString();
	private static String D2X = FileUtil.getFile(Resources.DATA2X).toString();
	private static String L2C = FileUtil.getFile(Resources.LOAD2C).toString();
	private static String L2X = FileUtil.getFile(Resources.LOAD2X).toString();

	@BeforeClass
	public static void initClass() throws JAXBException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		jdbc = ObjectFactoryUtil.getJdbcTypeBySrcXml(CX);
		jdlConnection = new JdlConnection(jdbc);
	}

	@Test
	public void execute() throws JAXBException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		assumeThat(jdbc.getDriver(), not(Resources.ENV_DRIVER));
		assumeThat(jdbc.getUrl(), not(Resources.ENV_URL));
		assumeThat(PropFileMaps.DEMO, is(true));
		demo();
	}

	public static void demo() throws JAXBException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		AppJdl appJdl;
		Connection connection = jdlConnection.getOpenConnection();
		SqlJdl.create(connection);
		// csv
		appJdl = new AppJdl(CX, D2C, L2C);
		appJdl.execute();
		SqlJdl.select(connection, true);
		SqlJdl.delete(connection);
		// xml
		appJdl = new AppJdl(CX, D2X, L2X);
		appJdl.execute();
		SqlJdl.select(connection, true);
		SqlJdl.delete(connection);
		// clear
		SqlJdl.drop(connection);
		jdlConnection.closeConnection();
	}
}
