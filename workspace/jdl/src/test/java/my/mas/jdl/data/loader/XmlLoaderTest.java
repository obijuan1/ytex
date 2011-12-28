package my.mas.jdl.data.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import my.mas.jdl.common.FileUtil;
import my.mas.jdl.data.base.JdlConnection;
import my.mas.jdl.data.xml.DomUtil;
import my.mas.jdl.data.xml.jaxb.ObjectFactoryUtil;
import my.mas.jdl.schema.xdl.XmlLoadType;
import my.mas.jdl.test.Resources;

import org.junit.BeforeClass;
import org.junit.Test;

public class XmlLoaderTest {
	private static final String CX = FileUtil.getFile(Resources.CONN_X).toString();
	private static final String D1X = FileUtil.getFile(Resources.DATA1X).toString();
	private static final String D2X = FileUtil.getFile(Resources.DATA2X).toString();
	private static final String L1X = FileUtil.getFile(Resources.LOAD1X).toString();
	private static final String L2X = FileUtil.getFile(Resources.LOAD2X).toString();
	private static final String SQL = "insert into tab_test (id,name,thekey,thevalue,code,descr) values (?,?,?,?,?,?)";

	@BeforeClass
	public static void initClass() throws JAXBException, FileNotFoundException {
		JdlConnection jdlConnection = new JdlConnection(ObjectFactoryUtil.getJdbcTypeBySrcXml(CX));
		jdlConnection.getClass();
	}

	@Test
	public void getSqlInsert() throws JAXBException, FileNotFoundException {
		XmlLoadType loader;
		XmlLoader xmlLoader;
		loader = ObjectFactoryUtil.getLoadTypeBySrcXml(L1X).getXml();
		xmlLoader = new XmlLoader(loader, DomUtil.srcToDocument(D1X));
		assertThat(xmlLoader.getSqlInsert(loader), is(SQL));
		loader = ObjectFactoryUtil.getLoadTypeBySrcXml(L2X).getXml();
		xmlLoader = new XmlLoader(loader, DomUtil.srcToDocument(D2X));
		assertThat(xmlLoader.getSqlInsert(loader), is(SQL));
	}
}
