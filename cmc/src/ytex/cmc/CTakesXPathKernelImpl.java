package ytex.cmc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ytex.kernel.ConceptSimilarityService;
import ytex.kernel.dao.KernelEvaluationDao;
import ytex.model.Document;

/**
 * This kernel uses XPath to get the objects.
 * XPath is very slow - profiling showed that most time is spent in calls to XPath.
 * Need to generate object graph from the get go.
 * Easier to do this with hibernate java object model.
 * 
 * @author vijay
 *
 */
public class CTakesXPathKernelImpl implements CMCKernel {
	private static final Log log = LogFactory.getLog(CTakesXPathKernelImpl.class);
	private static ThreadLocal<XPath> tlPath = new ThreadLocal<XPath>() {

		@Override
		protected XPath initialValue() {
			XPathFactory xpfactory = XPathFactory.newInstance();
			XPath xpath = xpfactory.newXPath();
			xpath.setNamespaceContext(new CASNamespaceContext());
			return xpath;
		}
		
	};
	SessionFactory cmcSessionFactory;

	public SessionFactory getCmcSessionFactory() {
		return cmcSessionFactory;
	}

	public void setCmcSessionFactory(SessionFactory cmcSessionFactory) {
		this.cmcSessionFactory = cmcSessionFactory;
	}

	SessionFactory sessionFactory;
	CacheManager cacheManager;
	Cache normCache;
	KernelEvaluationDao kernelEvaluationDao;
	ConceptSimilarityService conceptSimilarityService;

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	public KernelEvaluationDao getKernelEvaluationDao() {
		return kernelEvaluationDao;
	}

	public void setKernelEvaluationDao(KernelEvaluationDao kernelEvaluationDao) {
		this.kernelEvaluationDao = kernelEvaluationDao;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public NormKernel<Integer> cmcReportNormedKernel = new NormKernel<Integer>(
			new CMCReportKernel());

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.cmc.CTakesKernel#calculateSimilarity(int, int)
	 */
	public double calculateSimilarity(int instanceId1, int instanceId2) {
		return cmcReportNormedKernel.evaluateSimilarity(instanceId1,
				instanceId2);
	}

	public String getDocumentKey(org.w3c.dom.Document dom) {
		try {
			Node documentKey = (Node) getXPath().evaluate(
					"//types2:DocumentKey", dom, XPathConstants.NODE);
			if (documentKey != null) {
				return new StringBuilder(documentKey.getAttributes()
						.getNamedItem("uid").getNodeValue()).append("/")
						.append(
								documentKey.getAttributes().getNamedItem(
										"documentType").getNodeValue())
						.toString();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;

	}

	public String getNodeKey(Node node) {
		try {
			String documentKey = getDocumentKey(node.getOwnerDocument());
			if (documentKey != null) {
				return (new StringBuilder(documentKey)).append("/").append(
						node.getAttributes().getNamedItem("xmi:id")
								.getNodeValue()).toString();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public List<Object[]> getCMCDocuments(int instanceId) {
		Query q = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select documentTypeID, document from DocumentKeyAnnotation where uid = :uid");
		q.setInteger("uid", instanceId);
		return q.list();
	}

	public org.w3c.dom.Document getDOMDocument(Document d)
			throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = factory.newDocumentBuilder();
		GZIPInputStream gzis = null;
		try {
			gzis = new GZIPInputStream(new ByteArrayInputStream(d.getCas()));
			return builder.parse(gzis);
		} finally {
			if (gzis != null) {
				try {
					gzis.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public XPath getXPath() {
		return tlPath.get();
	}

	public static class CASNamespaceContext implements NamespaceContext {

		public String getNamespaceURI(String prefix) {
			if ("type".equals(prefix))
				return "http:///edu/mayo/bmi/uima/core/sentence/type.ecore";
			else if ("type2".equals(prefix))
				return "http:///edu/mayo/bmi/uima/core/ae/type.ecore";
			else if ("types2".equals(prefix))
				return "http:///ytex/vacs/uima/types.ecore";
			else if ("xmi".equals(prefix))
				return "http://www.omg.org/XMI";
			else if ("xml".equals(prefix))
				return XMLConstants.XML_NS_URI;
			else
				return XMLConstants.NULL_NS_URI;
		}

		// This method isn't necessary for XPath processing.
		public String getPrefix(String uri) {
			throw new UnsupportedOperationException();
		}

		// This method isn't necessary for XPath processing either.
		public Iterator getPrefixes(String uri) {
			throw new UnsupportedOperationException();
		}

	}

	public interface Kernel<T> {
		public String getKey(T o);

		public double evaluateSimilarity(T o1, T o2);
	}

	public class NormKernel<T> implements Kernel<T> {
		private Kernel<T> baseKernel;

		public NormKernel(Kernel<T> baseKernel) {
			this.baseKernel = baseKernel;
		}

		public double getNorm(T o1) {
			String key = getKey(o1);
			double norm = 0;
			if (o1 != null) {
				Element cachedNorm = normCache.get(key);
				if (cachedNorm == null) {
					norm = Math.sqrt(baseKernel.evaluateSimilarity(o1, o1));
					normCache.put(new Element(key, norm));
				} else {
					norm = (Double) cachedNorm.getObjectValue();
				}
			}
			return norm;
		}

		public double evaluateSimilarity(T o1, T o2) {
			double norm1 = getNorm(o1);
			double norm2 = getNorm(o2);
			if (norm1 != 0 && norm2 != 0)
				return baseKernel.evaluateSimilarity(o1, o2) / (norm1 * norm2);
			else
				return 0;
		}

		@Override
		public String getKey(T o) {
			return baseKernel.getKey(o);
		}
	}

	public class ConceptKernel implements Kernel<String> {

		@Override
		public double evaluateSimilarity(String o1, String o2) {
			return conceptSimilarityService.lch(o1, o2) * conceptSimilarityService.lin("cmc-ctakes", o1, o2);
		}

		@Override
		public String getKey(String o) {
			return o;
		}

	}

	public class NamedEntityKernel implements Kernel<Node> {
		public ConceptKernel conceptKernel = new ConceptKernel();

		/**
		 * //type2:OntologyConcept[@xmi:id = 504 or @xmi:id = 508]/@code
		 * 
		 * @param o1
		 * @return
		 * @throws Exception
		 */
		private List<String> getOntologyConcepts(Node o1) throws Exception {
			String ontologyConcepts[] = o1.getAttributes().getNamedItem(
					"ontologyConceptArr").getNodeValue().split("\\s");
			if (ontologyConcepts.length > 0) {
				List<String> concepts = new ArrayList<String>(ontologyConcepts.length);
				
				for (int i = 0; i < ontologyConcepts.length; i++) {
					StringBuilder strPathBuilder = new StringBuilder(
					"//type2:OntologyConcept[@xmi:id = ");
					strPathBuilder.append(ontologyConcepts[i]);
					strPathBuilder.append("]/@code");
					String value = getXPath().evaluate(strPathBuilder.toString(), o1.getOwnerDocument());
					if(value != null && value.length() > 0) {
						concepts.add(value);
					}
				}
				return concepts;
//					if (i < ontologyConcepts.length - 1) {
//						strPathBuilder.append(" or @xmi:id = ");
//					}
//				}
//				strPathBuilder.append("]/@code");
//				return (NodeList) getXPath().evaluate(
//						strPathBuilder.toString(), o1.getOwnerDocument(),
//						XPathConstants.NODESET);
			} else
				return null;
		}

		@Override
		public double evaluateSimilarity(Node o1, Node o2) {
			try {
				double dSim = 0;
				List<String> cuis1 = getOntologyConcepts(o1);
				List<String> cuis2 = getOntologyConcepts(o2);
				for(String cui1 : cuis1) {
					for(String cui2 : cuis2) {
						dSim += conceptKernel.evaluateSimilarity(cui1, cui2);
					}
				}
				
//				NodeList cuis1 = getOntologyConcepts(o1);
//				NodeList cuis2 = getOntologyConcepts(o2);
//				if (cuis1 != null && cuis2 != null) {
//					for (int i1 = 0; i1 < cuis1.getLength(); i1++) {
//						for (int i2 = 0; i2 < cuis2.getLength(); i2++) {
//							
//							if (cuis1.item(i1).getNodeValue() != null
//									&& cuis2.item(i2).getNodeValue() != null)
//								dSim += conceptKernel.evaluateSimilarity(cuis1
//										.item(i1).getNodeValue(), cuis2
//										.item(i2).getNodeValue());
//						}
//					}
//				}
				return dSim;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getKey(Node o) {
			return getNodeKey(o);
		}
	}

	public class SentenceKernel implements Kernel<Node> {
		private Kernel<Node> delegateKernel = new NamedEntityKernel();

		private NodeList getNamedEntities(Node o1) throws Exception {
			StringBuilder pathBuilder = new StringBuilder(
					"//type2:NamedEntity[@begin >= ");
			pathBuilder.append(o1.getAttributes().getNamedItem("begin")
					.getNodeValue());
			pathBuilder.append(" and @end <=");
			pathBuilder.append(o1.getAttributes().getNamedItem("end")
					.getNodeValue());
			pathBuilder.append("]");
			String strPath = pathBuilder.toString();
			return (NodeList) getXPath().evaluate(strPath,
					o1.getOwnerDocument(), XPathConstants.NODESET);
		}

		@Override
		public double evaluateSimilarity(Node o1, Node o2) {
//			if (log.isDebugEnabled()) {
//				log.debug("sentence1="
//						+ o1.getAttributes().getNamedItem("xmi:id")
//								.getNodeValue()
//						+ ",sentence2="
//						+ o2.getAttributes().getNamedItem("xmi:id")
//								.getNodeValue());
//			}
			try {
				double dSim = 0;
				NodeList namedEntities1 = getNamedEntities(o1);
				NodeList namedEntities2 = getNamedEntities(o2);
				for (int i1 = 0; i1 < namedEntities1.getLength(); i1++) {
					for (int i2 = 0; i2 < namedEntities2.getLength(); i2++) {
						dSim += delegateKernel.evaluateSimilarity(
								namedEntities1.item(i1), namedEntities2
										.item(i2));
					}
				}
				return dSim;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getKey(Node o) {
			return getNodeKey(o);
		}

	}

	public class DocumentKernel implements Kernel<Document> {
		private Kernel<Node> sentenceKernel = new NormKernel<Node>(
				new SentenceKernel());

		public String getKey(Document d1) {
			return Integer.toString(d1.getDocumentID());
		}

		public double evaluateSimilarity(Document o1, Document o2) {
			try {
				double dSim = 0;
				XPath xpath = getXPath();
				XPathExpression expr = xpath.compile("//type:Sentence");
				org.w3c.dom.Document dom1;
				dom1 = getDOMDocument(o1);
				org.w3c.dom.Document dom2 = getDOMDocument(o2);
				NodeList nodes1 = (NodeList) expr.evaluate(dom1,
						XPathConstants.NODESET);
				NodeList nodes2 = (NodeList) expr.evaluate(dom2,
						XPathConstants.NODESET);
				for (int i1 = 0; i1 < nodes1.getLength(); i1++) {
					for (int i2 = 0; i2 < nodes2.getLength(); i2++) {
						dSim += sentenceKernel.evaluateSimilarity(nodes1
								.item(i1), nodes2.item(i2));
					}
				}
				return dSim;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				else
					throw new RuntimeException(e);
			}
		}
	}

	public class CMCReportKernel implements Kernel<Integer> {
		private Kernel<Document> delegateKernel = new NormKernel<Document>(
				new DocumentKernel());

		public String getKey(Integer instanceId) {
			return instanceId.toString();
		}

		public double evaluateSimilarity(Integer instanceId1,
				Integer instanceId2) {
			double dSim = 0;
			List<Object[]> docs1 = getCMCDocuments(instanceId1);
			List<Object[]> docs2 = getCMCDocuments(instanceId2);
			for (Object[] typeDoc1 : docs1) {
				int documentTypeId1 = (Integer) typeDoc1[0];
				Document d1 = (Document) typeDoc1[1];
				for (Object[] typeDoc2 : docs2) {
					int documentTypeId2 = (Integer) typeDoc2[0];
					Document d2 = (Document) typeDoc2[1];
					if (documentTypeId1 == documentTypeId2) {
						dSim += delegateKernel.evaluateSimilarity(d1, d2);
					}
				}
			}
			return dSim;
		}
	}

	public void init() {
		normCache = cacheManager.getCache("normCache");
	}

	public void evaluateAllCMC() {
		Query q = cmcSessionFactory.getCurrentSession().createQuery(
				"select documentId from CMCDocument order by documentId asc");
		q.setMaxResults(10);
		List<Integer> documentIds = q.list();
		for (int i = 0; i < documentIds.size(); i++) {
			for (int j = i; j < documentIds.size(); j++) {
				if (i != j) {
					int instanceId1 = documentIds.get(i);
					int instanceId2 = documentIds.get(j);
					kernelEvaluationDao.storeKernel("cmc-ctakes", instanceId1,
							instanceId2, this.calculateSimilarity(instanceId1,
									instanceId2));
				}
			}
		}
	}
}
