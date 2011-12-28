package my.mas.jdl.test;

import java.util.Properties;

import my.mas.jdl.common.PropFile;

/**
 * Access to maps.properties file.
 * 
 * @author mas
 */
public class PropFileMaps {
	private static final boolean INTERNAL = true;
	private static final String SRC_FILE = "/maps.properties";
	private static Properties properties = PropFile.getProperties(SRC_FILE, INTERNAL);

	/**
	 * @param property
	 *            the property to get
	 * @return
	 */
	public static final String getProperty(final String property) {
		return PropFile.getProperty(properties, property);
	}

	/**
	 * Reset the properties.
	 */
	public static final void reset() {
		properties = PropFile.getProperties(SRC_FILE, INTERNAL);
	}

	public static final String ID = getProperty("id");
	public static final String KEY = getProperty("key");

	// If true you can use: mvn clean test -P jdbc,h2
	public static final boolean DEMO = Boolean.parseBoolean(getProperty("demo"));
}
