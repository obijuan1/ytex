package my.mas.jdl.data.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import my.mas.jdl.common.FileUtil;
import my.mas.jdl.test.Resources;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class DomParserTest {
	private static final String CX = FileUtil.getFile(Resources.CONN_X).toString();
	@DataPoint
	public static String L1C = Resources.LOAD1C;
	@DataPoint
	public static String L1X = Resources.LOAD1X;
	@DataPoint
	public static String L2C = Resources.LOAD2C;
	@DataPoint
	public static String L2X = Resources.LOAD2X;

	@Theory
	public void getRoot(String xml) {
		xml = FileUtil.getFile(xml).toString();
		DomParser dom;
		dom = new DomParser(CX);
		assertThat(dom.getRoot().getTagName(), is(Resources.ROOT_CONN));
		dom = new DomParser(xml);
		assertThat(dom.getRoot().getTagName(), is(Resources.ROOT_LOAD));
	}
}
