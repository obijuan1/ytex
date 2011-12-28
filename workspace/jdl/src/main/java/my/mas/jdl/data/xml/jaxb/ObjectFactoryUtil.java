package my.mas.jdl.data.xml.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import my.mas.jdl.schema.xdl.LoadType;
import my.mas.jdl.schema.xdl.ConnType;
import my.mas.jdl.schema.xdl.JdbcType;

/**
 * Utility to mange JAXB factory.
 * 
 * @author mas
 */
public final class ObjectFactoryUtil {
	private static ObjectFactoryBind objectFactoryMapping;

	private ObjectFactoryUtil() {
	}

	private static ObjectFactoryBind getObjectFactoryMapping() throws JAXBException {
		return (objectFactoryMapping == null) ? new ObjectFactoryBind() : objectFactoryMapping;
	}

	private static Object getJAXBElement(final Object obj) {
		return (obj == null) ? obj : ((JAXBElement<?>) obj).getValue();
	}

	private static Object getJAXBElementBySrcXml(final String srcXml) throws JAXBException {
		return getJAXBElement(getObjectFactoryMapping().unmarshalSrcXml(srcXml));
	}

	private static Object getJAXBElementByStrXml(final String strXml) throws JAXBException {
		return getJAXBElement(getObjectFactoryMapping().unmarshalStrXml(strXml));
	}

	/**
	 * @param srcXml
	 *            the srcXml to manage
	 * @return the jdbcType
	 * @throws JAXBException
	 *             exception
	 */
	public static JdbcType getJdbcTypeBySrcXml(final String srcXml) throws JAXBException {
		return getConnTypeBySrcXml(srcXml).getJdbc();
	}

	/**
	 * @param strXml
	 *            the strXml to manage
	 * @return the jdbcType
	 * @throws JAXBException
	 *             exception
	 */
	public static JdbcType getJdbcTypeByStrXml(final String strXml) throws JAXBException {
		return getConnTypeByStrXml(strXml).getJdbc();
	}

	/**
	 * @param srcXml
	 *            the srcXml to manage
	 * @return the connType
	 * @throws JAXBException
	 *             exception
	 */
	public static ConnType getConnTypeBySrcXml(final String srcXml) throws JAXBException {
		return (ConnType) getJAXBElementBySrcXml(srcXml);
	}

	/**
	 * @param strXml
	 *            the strXml to manage
	 * @return the connType
	 * @throws JAXBException
	 *             exception
	 */
	public static ConnType getConnTypeByStrXml(final String strXml) throws JAXBException {
		return (ConnType) getJAXBElementByStrXml(strXml);
	}

	/**
	 * @param srcXml
	 *            the srcXml to manage
	 * @return the loadType
	 * @throws JAXBException
	 *             exception
	 */
	public static LoadType getLoadTypeBySrcXml(final String srcXml) throws JAXBException {
		return (LoadType) getJAXBElementBySrcXml(srcXml);
	}

	/**
	 * @param strXml
	 *            the strXml to manage
	 * @return the loadType
	 * @throws JAXBException
	 *             exception
	 */
	public static LoadType getLoadTypeByStrXml(final String strXml) throws JAXBException {
		return (LoadType) getJAXBElementByStrXml(strXml);
	}
}
