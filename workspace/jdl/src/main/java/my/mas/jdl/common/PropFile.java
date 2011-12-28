package my.mas.jdl.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Access to a properties file.
 * 
 * @author mas
 */
public class PropFile {
	/**
	 * @param srcFile
	 *            the srcFile to load
	 * @param internal
	 *            where get the file
	 * @return the properties
	 */
	public static final Properties getProperties(final String srcFile, final boolean internal) {
		Properties properties = new Properties();
		try {
			if (internal) {
				properties.load(PropFile.class.getResourceAsStream(srcFile));
			} else {
				properties.load(new FileInputStream(srcFile));
			}
			return properties;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param properties
	 *            the properties to set
	 * @param property
	 *            the property to get
	 * @return the property
	 */
	public static final String getProperty(final Properties properties, final String property) {
		try {
			return properties.getProperty(property).trim();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
}
