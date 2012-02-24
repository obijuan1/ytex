package ytex.mesh;

import javax.sql.DataSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;

import ytex.kernel.KernelContextHolder;

/**
 * load mesh desc2012.xml and supp2012.xml into database
 * 
 * @author vijay
 *
 */
public class MeshLoader {
	private SimpleJdbcTemplate jdbcTemplate;

	public MeshLoader() {
		DataSource ds = KernelContextHolder.getApplicationContext().getBean(
				DataSource.class);
		jdbcTemplate = new SimpleJdbcTemplate(ds);
	}

	protected void insertConcept(String descriptorUI, String conceptUI,
			String conceptUMLSUI, String conceptString) {
		try {
		jdbcTemplate
				.update("insert into mesh_concept (	descriptorUI, conceptUI, conceptUMLSUI, conceptString) values (?, ?,?,?)",
						descriptorUI, conceptUI, conceptUMLSUI, conceptString);
		} catch(Exception e) {
			System.out.println("error adding mesh_concept: descriptorUI=" + descriptorUI + ", conceptString=" + conceptString);
			throw new RuntimeException(e);
		}
	}

	protected void insertTreeNumber(String descriptorUI, String treeNumber) {
		jdbcTemplate
				.update("insert into mesh_treenumber (	descriptorUI, treeNumber) values (?, ?)",
						descriptorUI, treeNumber);
	}

	protected void insertPharmAction(String descriptorUI, String pharmActionUI) {
		jdbcTemplate
				.update("insert into mesh_hier ( parUI, chdUI, rel) values (?, ?, 'pharm')",
						pharmActionUI, descriptorUI);
	}
	protected void insertHeader(String descriptorUI, String headerUI) {
		// tro, tje asterix
		String parUI = headerUI.startsWith("*") ? headerUI.substring(1) : headerUI;
		jdbcTemplate
				.update("insert into mesh_hier ( parUI, chdUI, rel) values (?, ?, 'head')",
						parUI, descriptorUI);
	}
	/**
	 * process something like:
	 * 
	 * 
	 * <pre>
	 * <DescriptorRecord DescriptorClass = "1">
	 *   <DescriptorUI>D019821</DescriptorUI>
	 *   ...
	 * 	 <PharmacologicalAction>
	 *       <DescriptorReferredTo>
	 *        <DescriptorUI>D000960</DescriptorUI>
	 *         <DescriptorName>
	 *          <String>Hypolipidemic Agents</String>
	 *         </DescriptorName>
	 *       </DescriptorReferredTo>
	 *      </PharmacologicalAction>
	 * </pre>
	 * 
	 * @author vijay
	 * 
	 */
	public class PharmActionAnnoHandler extends DefaultHandler {
		String descriptorUI;
		String cdata;
		boolean bFindFirstDescriptorUI = false;
		boolean bFindPharmDescriptorUI = false;

		public void startElement(String namespace, String localName,
				String qName, Attributes atts) {
			if (qName.equals("DescriptorRecord")
					|| qName.equals("SupplementalRecord")) {
				descriptorUI = null;
				bFindFirstDescriptorUI = true;
			} else if ((qName.equals("DescriptorUI") || qName
					.equals("SupplementalRecordUI"))
					&& (bFindFirstDescriptorUI || bFindPharmDescriptorUI)) {
				cdata = new String();
			} else if (qName.equals("PharmacologicalAction")) {
				bFindPharmDescriptorUI = true;
			}
		}

		public void characters(char[] ch, int start, int length) {
			if (cdata != null) {
				cdata += new String(ch, start, length);
			}
		}

		public void endElement(String namespace, String localName, String qName) {
			if (qName.equals("DescriptorRecord")
					|| qName.equals("SupplementalRecord")) {
				descriptorUI = null;
				cdata = null;
				bFindFirstDescriptorUI = false;
			} else if ((qName.equals("DescriptorUI") || qName
					.equals("SupplementalRecordUI")) && bFindFirstDescriptorUI) {
				descriptorUI = cdata;
				cdata = null;
				bFindFirstDescriptorUI = false;
			} else if (qName.equals("DescriptorUI") && bFindPharmDescriptorUI) {
				insertPharmAction(descriptorUI, cdata);
				cdata = null;
				bFindPharmDescriptorUI = false;
			}
		}
	}

	/**
	 * find hypernyms of supplemental concepts - HeadingMappedTo elements
	 * 
	 * @author vijay
	 * 
	 */
	public class HeadingAnnoHandler extends DefaultHandler {
		String descriptorUI;
		String cdata;
		boolean bFindFirstDescriptorUI = false;
		boolean bFindSupplementalDescriptorUI = false;

		public void startElement(String namespace, String localName,
				String qName, Attributes atts) {
			if (qName.equals("SupplementalRecord")) {
				descriptorUI = null;
				bFindSupplementalDescriptorUI = true;
			} else if (qName.equals("SupplementalRecordUI")
					&& bFindSupplementalDescriptorUI) {
				cdata = new String();
			} else if (qName.equals("HeadingMappedTo")) {
				bFindFirstDescriptorUI = true;
			} else if (qName.equals("DescriptorUI") && bFindFirstDescriptorUI) {
				cdata = new String();
			}
		}

		public void characters(char[] ch, int start, int length) {
			if (cdata != null) {
				cdata += new String(ch, start, length);
			}
		}

		public void endElement(String namespace, String localName, String qName) {
			if (qName.equals("SupplementalRecord")) {
				descriptorUI = null;
				cdata = null;
				bFindFirstDescriptorUI = false;
				bFindSupplementalDescriptorUI = false;
			} else if (qName.equals("SupplementalRecordUI")
					&& bFindSupplementalDescriptorUI) {
				descriptorUI = cdata;
				cdata = null;
				bFindSupplementalDescriptorUI = false;
			} else if (qName.equals("DescriptorUI") && bFindFirstDescriptorUI) {
				insertHeader(descriptorUI, cdata);
				cdata = null;
				bFindFirstDescriptorUI = false;
			}
		}
	}

	public class TreeNumberAnnoHandler extends DefaultHandler {
		String descriptorUI;
		String cdata;
		boolean bFindFirstDescriptorUI = false;

		public void startElement(String namespace, String localName,
				String qName, Attributes atts) {
			if (qName.equals("DescriptorRecord")) {
				descriptorUI = null;
				bFindFirstDescriptorUI = true;
			} else if (qName.equals("DescriptorUI") && bFindFirstDescriptorUI) {
				cdata = new String();
			} else if (qName.equals("TreeNumber")) {
				cdata = new String();
			}
		}

		public void characters(char[] ch, int start, int length) {
			if (cdata != null) {
				cdata += new String(ch, start, length);
			}
		}

		public void endElement(String namespace, String localName, String qName) {
			if (qName.equals("DescriptorRecord")) {
				descriptorUI = null;
				cdata = null;
				bFindFirstDescriptorUI = false;
			} else if (qName.equals("DescriptorUI") && bFindFirstDescriptorUI) {
				descriptorUI = cdata;
				cdata = null;
				bFindFirstDescriptorUI = false;
			} else if (qName.equals("TreeNumber")) {
				insertTreeNumber(descriptorUI, cdata);
				cdata = null;
			}
		}
	}

	public class ConceptAnnoHandler extends DefaultHandler {

		String descriptorUI;
		String conceptUI;
		String conceptUMLSUI;
		String conceptString;
		String cdata;
		boolean bInConceptName = false;
		boolean bFindFirstDescriptorUI = false;

		public void startElement(String namespace, String localName,
				String qName, Attributes atts) {
			if (qName.equals("DescriptorRecord")
					|| qName.equals("SupplementalRecord")) {
				descriptorUI = null;
				bInConceptName = false;
				bFindFirstDescriptorUI = true;
			} else if (qName.equals("Concept")) {
				conceptUI = null;
				conceptUMLSUI = null;
				conceptString = null;
				cdata = null;
				bInConceptName = false;
				bFindFirstDescriptorUI = false;
			} else if ((qName.equals("DescriptorUI") || qName
					.equals("SupplementalRecordUI")) && bFindFirstDescriptorUI) {
				cdata = new String();
			} else if (qName.equals("ConceptUI")) {
				cdata = new String();
			} else if (qName.equals("ConceptUMLSUI")) {
				cdata = new String();
			} else if (qName.equals("ConceptName")) {
				bInConceptName = true;
			} else if (qName.equals("String") && bInConceptName) {
				cdata = new String();
			}
		}

		public void characters(char[] ch, int start, int length) {
			if (cdata != null) {
				cdata += new String(ch, start, length);
			}
		}

		public void endElement(String namespace, String localName, String qName) {
			if (qName.equals("DescriptorRecord")
					|| qName.equals("SupplementalRecord")) {
			} else if (qName.equals("Concept")) {
				insertConcept(descriptorUI, conceptUI, conceptUMLSUI,
						conceptString);
			} else if ((qName.equals("DescriptorUI") || qName
					.equals("SupplementalRecordUI")) && bFindFirstDescriptorUI) {
				descriptorUI = cdata;
				cdata = null;
				bFindFirstDescriptorUI = false;
			} else if (qName.equals("ConceptUI")) {
				conceptUI = cdata;
				cdata = null;
			} else if (qName.equals("ConceptUMLSUI")) {
				conceptUMLSUI = cdata;
				cdata = null;
			} else if (qName.equals("String") && bInConceptName) {
				conceptString = cdata;
				cdata = null;
			} else if (qName.equals("ConceptName")) {
				bInConceptName = false;
			}
		}
	}

	public static void main(String args[]) throws Exception {
		MeshLoader l = new MeshLoader();
		l.process(args[0], args[1]);
	}

	public void process(String descXML, String suppXML) throws Exception {
		System.out.println("Processing URL " + descXML);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		// parse desc2012.xml
		ParserAdapter pa = new ParserAdapter(sp.getParser());
		pa.setContentHandler(new ConceptAnnoHandler());
		pa.parse(descXML);
		pa = new ParserAdapter(sp.getParser());
		pa.setContentHandler(new TreeNumberAnnoHandler());
		pa.parse(descXML);
		pa = new ParserAdapter(sp.getParser());
		pa.setContentHandler(new PharmActionAnnoHandler());
		pa.parse(descXML);	
		// parse supp2012.xml
		pa.setContentHandler(new ConceptAnnoHandler());
		pa.parse(suppXML);
		pa = new ParserAdapter(sp.getParser());
		pa.setContentHandler(new HeadingAnnoHandler());
		pa.parse(suppXML);
		pa = new ParserAdapter(sp.getParser());
	}
}
