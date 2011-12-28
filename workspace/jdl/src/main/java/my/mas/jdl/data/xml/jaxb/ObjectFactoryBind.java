package my.mas.jdl.data.xml.jaxb;

import java.io.File;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import my.mas.jdl.schema.xdl.ObjectFactory;

/**
 * Bind JAXB factory.
 * 
 * @author mas
 */
public class ObjectFactoryBind {
	private Unmarshaller unmarshaller;

	/**
	 * @throws JAXBException
	 *             exception
	 */
	public ObjectFactoryBind() throws JAXBException {
		unmarshaller = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName()).createUnmarshaller();
	}

	/**
	 * @param srcXml
	 *            the srcXml to unmarshal
	 * @return the object unmarshalled
	 * @throws JAXBException
	 *             exception
	 */
	public final Object unmarshalSrcXml(final String srcXml) throws JAXBException {
		try {
			return unmarshaller.unmarshal(new File(srcXml));
		} catch (JAXBException e) {
			throw e;
		}
	}

	/**
	 * @param strXml
	 *            the strXml to unmarshal
	 * @return the object unmarshalled
	 * @throws JAXBException
	 *             exception
	 */
	public final Object unmarshalStrXml(final String strXml) throws JAXBException {
		try {
			return unmarshaller.unmarshal(new StringReader(strXml));
		} catch (JAXBException e) {
			throw e;
		}
	}
}
