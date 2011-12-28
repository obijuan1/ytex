package my.mas.jdl.data.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;

import javax.xml.validation.Schema;

import my.mas.jdl.AppJdl;
import my.mas.jdl.common.FileUtil;
import my.mas.jdl.test.Resources;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ValidationTest {
	private static final URL XSD = AppJdl.XSD;
	private static Validation validation;
	@DataPoint
	public static String CX = Resources.CONN_X;
	@DataPoint
	public static String L1C = Resources.LOAD1C;
	@DataPoint
	public static String L1X = Resources.LOAD1X;
	@DataPoint
	public static String L2C = Resources.LOAD2C;
	@DataPoint
	public static String L2X = Resources.LOAD2X;

	@BeforeClass
	public static void initClass() {
		Schema schema = SchemaUtil.urlToSchema(XSD);
		validation = new Validation(schema);
	}

	@Test
	public void setSchema() {
		validation.setSchema(SchemaUtil.urlToSchema(XSD));
		assertThat(validation.getError(), nullValue());
		validation.setSchema(XSD.getPath());
		assertThat(validation.getError(), nullValue());
	}

	@Theory
	public void setDocument(String xml) {
		xml = FileUtil.getFile(xml).toString();
		validation.setDocument(DomUtil.srcToDocument(xml));
		assertThat(validation.succeed(), is(true));
		validation.setDocument(xml);
		assertThat(validation.succeed(), is(true));
	}

	@After
	public void getError() {
		assertThat(validation.getError(), nullValue());
	}

	@Theory
	public void succeed(String xml) {
		xml = FileUtil.getFile(xml).toString();
		validation.setDocument(DomUtil.srcToDocument(xml));
		assertThat(validation.succeed(), is(true));
		validation.setDocument(xml);
		assertThat(validation.succeed(), is(true));
	}
}
