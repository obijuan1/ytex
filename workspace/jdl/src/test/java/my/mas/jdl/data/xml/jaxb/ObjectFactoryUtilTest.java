package my.mas.jdl.data.xml.jaxb;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import my.mas.jdl.common.FileUtil;
import my.mas.jdl.schema.xdl.ConnType;
import my.mas.jdl.schema.xdl.JdbcType;
import my.mas.jdl.schema.xdl.LoadType;
import my.mas.jdl.test.Resources;

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public final class ObjectFactoryUtilTest {
	private static final String CX = FileUtil.getFile(Resources.CONN_X).toString();
	@DataPoint
	public static String L1C = Resources.LOAD1C;
	@DataPoint
	public static String L1X = Resources.LOAD1X;
	@DataPoint
	public static String L2C = Resources.LOAD2C;
	@DataPoint
	public static String L2X = Resources.LOAD1X;

	public void getJdbcTypeBySrcXml() throws JAXBException {
		Object obj = ObjectFactoryUtil.getJdbcTypeBySrcXml(CX);
		assertThat(obj, instanceOf(JdbcType.class));
	}

	@Test(expected = UnmarshalException.class)
	public void getJdbcTypeByStrXml() throws JAXBException {
		ObjectFactoryUtil.getJdbcTypeByStrXml("<root />");
	}

	public void getConnTypeBySrcXml() throws JAXBException {
		Object obj = ObjectFactoryUtil.getJdbcTypeBySrcXml(CX);
		assertThat(obj, instanceOf(ConnType.class));
	}

	@Test(expected = UnmarshalException.class)
	public void getConnTypeByStrXml() throws JAXBException {
		ObjectFactoryUtil.getConnTypeByStrXml("<root />");
	}

	@Theory
	public void getBindTypeBySrcXml(String xml) throws JAXBException {
		xml = FileUtil.getFile(xml).toString();
		Object obj = ObjectFactoryUtil.getLoadTypeBySrcXml(xml);
		assertThat(obj, instanceOf(LoadType.class));
	}

	@Test(expected = UnmarshalException.class)
	public void getBindTypeByStrXml() throws JAXBException {
		ObjectFactoryUtil.getJdbcTypeByStrXml("<root />");
	}
}
