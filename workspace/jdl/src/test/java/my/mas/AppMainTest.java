package my.mas;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import my.mas.AppMain;
import my.mas.jdl.common.FileUtil;
import my.mas.jdl.data.base.JdlConnection;
import my.mas.jdl.data.xml.jaxb.ObjectFactoryUtil;
import my.mas.jdl.schema.xdl.JdbcType;
import my.mas.jdl.test.PropFileMaps;
import my.mas.jdl.test.Resources;
import my.mas.jdl.test.SqlJdl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppMainTest {
	private static JdbcType jdbc;
	private static JdlConnection jdlConnection;
	private static String CX = FileUtil.getFile(Resources.CONN_X).toString();
	private static String D1C = FileUtil.getFile(Resources.DATA1C).toString();
	private static String D1X = FileUtil.getFile(Resources.DATA1X).toString();
	private static String L1C = FileUtil.getFile(Resources.LOAD1C).toString();
	private static String L1X = FileUtil.getFile(Resources.LOAD1X).toString();
	private static String C = "-" + AppMain.OPT_XDL_CONN;
	private static String D = "-" + AppMain.OPT_XDL_DATA;
	private static String L = "-" + AppMain.OPT_XDL_LOAD;

	@BeforeClass
	public static void initClass() throws JAXBException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		jdbc = ObjectFactoryUtil.getJdbcTypeBySrcXml(CX);
		jdlConnection = new JdlConnection(jdbc);
	}

	@Test
	public void parsingCLI() throws ParseException {
		String[] args;
		CommandLine cl;
		args = new String[] { C, "conn.xml", D, "data.xml", L, "load.csv" };
		cl = AppMain.parsingCLI(args);
		assertThat(cl.getOptionValue(AppMain.OPT_XDL_CONN), is("conn.xml"));
		assertThat(cl.getOptionValue(AppMain.OPT_XDL_DATA), is("data.xml"));
		assertThat(cl.getOptionValue(AppMain.OPT_XDL_LOAD), is("load.csv"));
		args = new String[] { C, "conn.xml", D, "data.xml", L, "load.xml" };
		cl = AppMain.parsingCLI(args);
		assertThat(cl.getOptionValue(AppMain.OPT_XDL_CONN), is("conn.xml"));
		assertThat(cl.getOptionValue(AppMain.OPT_XDL_DATA), is("data.xml"));
		assertThat(cl.getOptionValue(AppMain.OPT_XDL_LOAD), is("load.xml"));
	}

	@Test
	public void main() throws JAXBException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		assumeThat(jdbc.getDriver(), not(Resources.ENV_DRIVER));
		assumeThat(jdbc.getUrl(), not(Resources.ENV_URL));
		assumeThat(PropFileMaps.DEMO, is(true));
		demo();
	}

	public static void demo() throws JAXBException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String[] args;
		Connection connection = jdlConnection.getOpenConnection();
		SqlJdl.create(connection);
		// csv
		args = new String[] { C, CX, D, D1C, L, L1C };
		AppMain.main(args);
		SqlJdl.select(connection, true);
		SqlJdl.delete(connection);
		// xml
		args = new String[] { C, CX, D, D1X, L, L1X };
		AppMain.main(args);
		SqlJdl.select(connection, true);
		SqlJdl.delete(connection);
		// clear
		SqlJdl.drop(connection);
		jdlConnection.closeConnection();
	}
}
