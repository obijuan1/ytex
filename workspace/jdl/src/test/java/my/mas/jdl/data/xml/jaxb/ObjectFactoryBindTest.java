package my.mas.jdl.data.xml.jaxb;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import my.mas.jdl.common.FileUtil;
import my.mas.jdl.test.Resources;

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ObjectFactoryBindTest {
	@DataPoint
	public static String CX = Resources.CONN_X;
	@DataPoint
	public static String L1C = Resources.LOAD1C;
	@DataPoint
	public static String L1X = Resources.LOAD1X;
	@DataPoint
	public static String L2C = Resources.LOAD2C;
	@DataPoint
	public static String L2X = Resources.LOAD1X;

	@Theory
	public void unmarshalSrcXml(String xml) throws JAXBException {
		xml = FileUtil.getFile(xml).toString();
		Object obj = new ObjectFactoryBind().unmarshalSrcXml(xml);
		assertThat(obj, instanceOf(JAXBElement.class));
	}

	@Test(expected = UnmarshalException.class)
	public void unmarshalStrXml() throws JAXBException {
		new ObjectFactoryBind().unmarshalStrXml("<root />");
	}
}
