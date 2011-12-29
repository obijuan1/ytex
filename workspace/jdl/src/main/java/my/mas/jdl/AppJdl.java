package my.mas.jdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import my.mas.jdl.data.base.JdlConnection;
import my.mas.jdl.data.loader.CsvLoader;
import my.mas.jdl.data.loader.XmlLoader;
import my.mas.jdl.data.xml.DomUtil;
import my.mas.jdl.data.xml.SchemaUtil;
import my.mas.jdl.data.xml.Validation;
import my.mas.jdl.data.xml.jaxb.ObjectFactoryUtil;
import my.mas.jdl.schema.xdl.CsvLoadType;
import my.mas.jdl.schema.xdl.JdbcType;
import my.mas.jdl.schema.xdl.LoadType;
import my.mas.jdl.schema.xdl.XmlLoadType;

/**
 * Java data loader Application.
 * 
 * @author mas
 */
public class AppJdl {
	private String srcConn;
	private String srcData;
	private String srcLoad;
	public static final URL XSD = AppJdl.class.getResource("/xdl.xsd");

	/**
	 * @param srcConn
	 *            the conn file
	 * @param srcData
	 *            the data file
	 * @param srcLoad
	 *            the load file
	 */
	public AppJdl(String srcConn, String srcData, String srcLoad) {
		this.srcConn = srcConn;
		this.srcData = srcData;
		this.srcLoad = srcLoad;
	}

	/**
	 * Execute the loader of the data into the database.
	 */
	public void execute() {
		Validation validation = new Validation(SchemaUtil.urlToSchema(XSD), srcConn);
		if (validation.succeed()) {
			validation.setDocument(srcLoad);
			if (validation.succeed()) {
				try {
					JdbcType jdbc = ObjectFactoryUtil.getJdbcTypeBySrcXml(srcConn);
					LoadType load = ObjectFactoryUtil.getLoadTypeBySrcXml(srcLoad);
					JdlConnection jdlConnection = new JdlConnection(jdbc);
					CsvLoadType csv = load.getCsv();
					if (csv != null) {
						try {
							CsvLoader csvLoader = new CsvLoader(csv, new File(srcData));
							csvLoader.dataInsert(jdlConnection);
						} catch (FileNotFoundException e) {
							throw new RuntimeException(e);
						}
					}
					XmlLoadType xml = load.getXml();
					if (xml != null) {
						XmlLoader xPathParsing = new XmlLoader(xml, DomUtil.srcToDocument(srcData));
						xPathParsing.dataInsert(jdlConnection);
					}
				} catch (JAXBException e) {
					// TODO
					e.printStackTrace();
				}
			} else {
				System.err.println(validation.getError());
			}
		} else {
			System.err.println(validation.getError());
		}
	}
}
