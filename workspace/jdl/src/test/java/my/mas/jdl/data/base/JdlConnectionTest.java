package my.mas.jdl.data.base;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import my.mas.jdl.common.FileUtil;
import my.mas.jdl.data.xml.jaxb.ObjectFactoryUtil;
import my.mas.jdl.schema.xdl.JdbcType;
import my.mas.jdl.test.Resources;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JdlConnectionTest {
	private static JdbcType jdbc;
	private static JdlConnection jdlConnection;
	private static final String CX = FileUtil.getFile(Resources.CONN_X).toString();

	@BeforeClass
	public static void initClass() throws JAXBException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		jdbc = ObjectFactoryUtil.getJdbcTypeBySrcXml(CX);
		jdlConnection = new JdlConnection(jdbc);
	}

	@Test
	public void isConnected() throws SQLException {
		assertThat(jdlConnection.isConnected(), is(true));
	}

	@Before
	public void getOpenConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		assumeThat(jdbc.getDriver(), not(Resources.ENV_DRIVER));
		assumeThat(jdbc.getUrl(), not(Resources.ENV_URL));
		assertThat(jdlConnection.getOpenConnection().isClosed(), is(false));
	}

	@AfterClass
	public static void closeConnection() throws SQLException {
		jdlConnection.closeConnection();
		assertThat(jdlConnection.isConnected(), is(false));
	}

	@Test
	public void autoCommit() throws SQLException {
		jdlConnection.setAutoCommit(true);
		assertThat(jdlConnection.isAutoCommit(), is(true));
		jdlConnection.setAutoCommit(false);
		assertThat(jdlConnection.isAutoCommit(), is(false));
		jdlConnection.setAutoCommit(true);
		assertThat(jdlConnection.isAutoCommit(), is(true));
	}
}
