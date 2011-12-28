package my.mas.jdl.data.xml;

import java.io.File;
import java.io.StringReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * Utility to manage Schema.
 * 
 * @author mas
 */
public final class SchemaUtil {
	private SchemaUtil() {
	}

	/**
	 * @param srcXsd
	 *            the srcXsd to convert
	 * @return the schema
	 */
	public static Schema srcToSchema(final String srcXsd) {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema(new File(srcXsd));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param url
	 *            the url to convert
	 * @return the schema
	 */
	public static Schema urlToSchema(final URL url) {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema(url);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param strXsd
	 *            the strXsd to convert
	 * @return the schema
	 */
	public static Schema strToSchema(final String strXsd) {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema(new StreamSource(new StringReader(strXsd)));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
