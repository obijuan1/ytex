package my.mas.jdl.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Access to a properties file.
 * 
 * @author mas
 */
public class PropertiesFile {
	private Properties properties;

	/**
	 * External.
	 * 
	 * @param srcFile
	 *            the srcFile to load
	 * @throws IOException
	 *             exception
	 */
	public final void loadInputStream(final String srcFile) throws IOException {
		properties = new Properties();
		properties.load(new FileInputStream(srcFile));
	}

	/**
	 * Internal.
	 * 
	 * @param srcFile
	 *            the srcFile to load
	 * @throws IOException
	 *             exception
	 */
	public final void loadResourceAsStream(final String srcFile) throws IOException {
		properties = new Properties();
		properties.load(PropertiesFile.class.getResourceAsStream(srcFile));
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public final void setProperties(final Properties properties) {
		this.properties = properties;
	}

	/**
	 * @param property
	 *            the property to get
	 * @return the property
	 */
	public final String getProperty(final String property) {
		if (properties != null) {
			return properties.getProperty(property);
		}
		return null;
	}

	/**
	 * Clears this hashtable.
	 */
	public final void clear() {
		this.properties.clear();
	}
}
