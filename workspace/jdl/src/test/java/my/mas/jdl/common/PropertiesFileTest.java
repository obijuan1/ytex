package my.mas.jdl.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Properties;

import my.mas.jdl.test.Resources;

import org.junit.After;
import org.junit.Test;

public class PropertiesFileTest {
	private static final PropertiesFile propertiesFile = new PropertiesFile();

	@Test
	public void loadInputStream() throws IOException {
		propertiesFile.loadInputStream(FileUtil.getFile(Resources.MAPS_P).toString());
		assertThat(propertiesFile.getProperty(Resources.MAP_ID), is(Resources.MAP_NAME));
		assertThat(propertiesFile.getProperty(Resources.MAP_KEY), is(Resources.MAP_VALUE));
	}

	@Test(expected = IOException.class)
	public void loadInputStreamExecption() throws IOException {
		propertiesFile.loadInputStream("/" + Resources.MAPS_P);
	}

	@Test
	public void loadResourceAsStream() throws IOException {
		propertiesFile.loadResourceAsStream("/" + Resources.MAPS_P);
		assertThat(propertiesFile.getProperty(Resources.MAP_ID), is(Resources.MAP_NAME));
		assertThat(propertiesFile.getProperty(Resources.MAP_KEY), is(Resources.MAP_VALUE));
	}

	@Test(expected = NullPointerException.class)
	public void loadResourceAsStreamExecption() throws IOException {
		propertiesFile.loadResourceAsStream(FileUtil.getFile(Resources.MAPS_P).toString());
	}

	@Test
	public void property() {
		Properties properties = new Properties();
		properties.setProperty("code", "descr");
		propertiesFile.setProperties(properties);
		assertThat(propertiesFile.getProperty("code"), is("descr"));
	}

	@After
	public void clear() {
		propertiesFile.clear();
		assertThat(propertiesFile.getProperty(Resources.MAP_ID), nullValue());
		assertThat(propertiesFile.getProperty(Resources.MAP_KEY), nullValue());
		assertThat(propertiesFile.getProperty("code"), nullValue());
	}
}
