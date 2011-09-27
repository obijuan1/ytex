package ytex.cmc;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hibernate.SessionFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;

import ytex.cmc.model.CMCDocument;
import ytex.cmc.model.CMCDocumentCode;

public class DocumentLoaderImpl implements DocumentLoader {
	SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public class CMCHandler extends DefaultHandler {
		private String currTag;
		private String cdata;
		private CMCDocument cDoc;
		private String documentSet;

		public CMCHandler(String documentSet) {
			init();
			this.documentSet = documentSet;
		}

		public void init() {
			currTag = "";
			cdata = "";
		}

		public void startElement(String namespace, String localName,
				String qName, Attributes atts) {
			cdata = "";
			currTag = localName.toLowerCase();
			if (currTag.equals("doc")) {
				cDoc = new CMCDocument();
				cDoc.setDocumentSet(documentSet);
				int id = Integer.parseInt(atts.getValue("id"));
				cDoc.setDocumentId(id);
			} else if (currTag.equals("code")) {
				if (atts.getValue("origin").trim().equalsIgnoreCase(
						"CMC_MAJORITY")) {
					currTag = "code_target";
				}
			} else if (currTag.equals("text")) {
				String type = atts.getValue("type").trim();
				if (type.equalsIgnoreCase("CLINICAL_HISTORY")) {
					currTag = "text_clinical";
				} else if (type.equalsIgnoreCase("IMPRESSION")) {
					currTag = "text_radio";
				} else {
					System.err.println("Unknown text type: " + type);
				}
			}
		}

		public void characters(char[] ch, int start, int length) {
			cdata += new String(ch, start, length);
		}

		public void endElement(String uri, String localName, String qName) {
			cdata = cdata.trim().toLowerCase();
			if (localName.equalsIgnoreCase("doc")) {
				sessionFactory.getCurrentSession().save(cDoc);
				for(CMCDocumentCode code : cDoc.getDocumentCodes()) {
					sessionFactory.getCurrentSession().save(code);
				}
				cDoc = null;
				currTag = "";
			} else if (currTag.equalsIgnoreCase("text_clinical")) {
				cDoc.setClinicalHistory(cdata);
			} else if (currTag.equalsIgnoreCase("text_radio")) {
				cDoc.setImpression(cdata);
			} else if (currTag.equalsIgnoreCase("code_target")) {
				if (cdata.length() > 0) {
					CMCDocumentCode code = new CMCDocumentCode();
					code.setDocument(cDoc);
					code.setCode(cdata);
					cDoc.getDocumentCodes().add(code);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see ytex.cmc.DocumentLoader#process(java.lang.String, java.lang.String)
	 */
	public void process(String urlString, String documentSet) throws Exception {
		System.out.println("Processing URL " + urlString);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		ParserAdapter pa = new ParserAdapter(sp.getParser());
		pa.setContentHandler(new CMCHandler(documentSet));
		pa.parse(urlString);
	}

}
