package my.mas.jdl.data.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import my.mas.jdl.common.FileUtil;
import my.mas.jdl.data.base.JdlConnection;
import my.mas.jdl.data.xml.jaxb.ObjectFactoryUtil;
import my.mas.jdl.schema.xdl.CsvLoadType;
import my.mas.jdl.test.Resources;

import org.junit.BeforeClass;
import org.junit.Test;

public class CsvLoaderTest {
	private static final String CX = FileUtil.getFile(Resources.CONN_X).toString();
	private static final String D1C = FileUtil.getFile(Resources.DATA1C).toString();
	private static final String D2C = FileUtil.getFile(Resources.DATA2C).toString();
	private static final String L1C = FileUtil.getFile(Resources.LOAD1C).toString();
	private static final String L2C = FileUtil.getFile(Resources.LOAD2C).toString();
	private static final String SQL = "insert into tab_test (id,name,thekey,thevalue,code,descr) values (?,?,?,?,?,?)";

	@BeforeClass
	public static void initClass() throws JAXBException, FileNotFoundException {
		JdlConnection jdlConnection = new JdlConnection(ObjectFactoryUtil.getJdbcTypeBySrcXml(CX));
		jdlConnection.getClass();
	}

	@Test
	public void getSqlInsert() throws JAXBException, FileNotFoundException {
		CsvLoadType loader;
		CsvLoader csvLoader;
		loader = ObjectFactoryUtil.getLoadTypeBySrcXml(L1C).getCsv();
		csvLoader = new CsvLoader(loader, new File(D1C));
		assertThat(csvLoader.getSqlInsert(loader), is(SQL));
		loader = ObjectFactoryUtil.getLoadTypeBySrcXml(L2C).getCsv();
		csvLoader = new CsvLoader(loader, new File(D2C));
		assertThat(csvLoader.getSqlInsert(loader), is(SQL));
	}
}
