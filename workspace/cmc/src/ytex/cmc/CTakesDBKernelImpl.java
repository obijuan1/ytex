package ytex.cmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.ConceptSimilarityService;

/**
 */
public class CTakesDBKernelImpl implements CMCKernel {
	private static final Log log = LogFactory.getLog(CTakesDBKernelImpl.class);
	SessionFactory sessionFactory;
	CacheManager cacheManager;
	Cache normCache;
	Cache conceptSimCache;
	ConceptSimilarityService conceptSimilarityService;
	PlatformTransactionManager transactionManager;

	Map<Integer, Node> instanceTreeMap;

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

	public ConceptSimilarityService getConceptSimilarityService() {
		return conceptSimilarityService;
	}

	public void setConceptSimilarityService(
			ConceptSimilarityService conceptSimilarityService) {
		this.conceptSimilarityService = conceptSimilarityService;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	private Kernel instanceKernel = new NormKernel(new ConvolutionKernel(
			new CMCInstanceKeyGenerator()));
	private Kernel documentKernel = new NormKernel(new DocumentKernel(
			new CMCDocumentKeyGenerator()));
	private Kernel sentenceKernel = new NormKernel(new ConvolutionKernel(
			new SentenceKeyGenerator()));
	private Kernel namedEntityKernel = new ConvolutionKernel();
	private Kernel conceptKernel = new ConceptKernel();

	public class ConceptKernel implements Kernel {
		private String createKey(String c1, String c2) {
			if (c1.compareTo(c2) < 0) {
				return new StringBuilder(c1).append("-").append(c2).toString();
			} else {
				return new StringBuilder(c2).append("-").append(c1).toString();
			}
		}

		@Override
		public double evaluate(Object o1, Object o2) {
			String c1 = (String) ((Node) o1).getNode();
			String c2 = (String) ((Node) o2).getNode();
			if (c1.equals(c2))
				return 1;
			else {
				// look in cache
				String key = createKey(c1, c2);
				Element e = conceptSimCache.get(key);
				double d = 0;
				if (e != null) {
					// it's there
					d = (Double) e.getObjectValue();
				} else {
					// it's not there - put it there
					d = conceptSimilarityService.lch(c1, c2)
							* conceptSimilarityService
									.lin("cmc-ctakes", c1, c2);
					conceptSimCache.put(new Element(key, d));
				}
				return d;
			}
		}

		@Override
		public String cacheKey(Object o) {
			return null;
		}

	}

	public interface KeyGenerator {
		public String getKey(Object o);
	}

	public class DefaultKeyGenerator implements KeyGenerator {
		public String getKey(Object o) {
			return o.toString();
		}
	}

	public class CMCInstanceKeyGenerator implements KeyGenerator {
		public String getKey(Object o) {
			int nInstanceId = (Integer) o;
			return new StringBuilder("rep-").append(nInstanceId).toString();
		}
	}

	public class CMCDocumentKeyGenerator implements KeyGenerator {
		public String getKey(Object o) {
			Integer documentID = (Integer) ((Object[]) o)[1];
			return new StringBuilder("doc-").append(documentID).toString();
		}
	}

	public class SentenceKeyGenerator implements KeyGenerator {
		public String getKey(Object o) {
			Integer da = (Integer) o;
			return new StringBuilder("sent-").append(da).toString();
		}
	}

	public interface Kernel {
		double evaluate(Object c1, Object c2);

		String cacheKey(Object c);
	}

	public class ConvolutionKernel implements Kernel {
		private KeyGenerator keyGenerator;
		private int pow = 1;

		public double evaluate(Object c1, Object c2) {
			Node n1 = (Node) c1;
			Node n2 = (Node) c2;
			double d = 0;
			for (Node child1 : n1.getChildren()) {
				for (Node child2 : n2.getChildren()) {
					d += child1.getKernel().evaluate(child1, child2);
				}
			}
			return d;
		}

		public String cacheKey(Object c) {
			if (keyGenerator != null)
				return keyGenerator.getKey(((Node) c).getNode());
			else
				return null;
		}

		public ConvolutionKernel(KeyGenerator keyGenerator) {
			this.keyGenerator = keyGenerator;
		}
		public ConvolutionKernel(KeyGenerator keyGenerator, int pow) {
			this.keyGenerator = keyGenerator;
			this.pow = pow;
		}

		public ConvolutionKernel() {
		}
	}

	/**
	 * for comparing document sections - only recurse if both documents have the
	 * same section
	 * 
	 * @author vijay
	 * 
	 */
	public class DocumentKernel extends ConvolutionKernel {

		public DocumentKernel(KeyGenerator keyGenerator) {
			super(keyGenerator);
		}

		public double evaluate(Object c1, Object c2) {
			Node n1 = (Node) c1;
			Node n2 = (Node) c2;
			int sectionId1 = (Integer) ((Object[]) n1.getNode())[0];
			int sectionId2 = (Integer) ((Object[]) n2.getNode())[0];
			if (sectionId1 == sectionId2) {
				return super.evaluate(c1, c2);
			} else {
				return 0;
			}
		}
	}

	// public interface Relation<P,C> {
	// P getParent();
	// List<C> getChildren();
	// Kernel<C> getKernel();
	// }
	// @SuppressWarnings("hiding")
	// public class ConvolutionKernel<Relation<P, C>> implements
	// Kernel<Relation<P, C>> {
	//
	// @Override
	// public String cacheKey(P c) {
	// return null;
	// }
	//
	// @Override
	// public double evaluate(P c1, P c2) {
	// // TODO Auto-generated method stub
	// return 0;
	// }
	//		
	//		
	// }
	public class NormKernel implements Kernel {
		private Kernel baseKernel;

		public NormKernel(Kernel baseKernel) {
			this.baseKernel = baseKernel;
		}

		public double getNorm(Object o1) {
			String key = cacheKey(o1);
			double norm = 0;
			if (o1 != null) {
				Element cachedNorm = normCache.get(key);
				if (cachedNorm == null) {
					norm = Math.sqrt(baseKernel.evaluate(o1, o1));
					normCache.put(new Element(key, norm));
				} else {
					norm = (Double) cachedNorm.getObjectValue();
				}
			}
			return norm;
		}

		public double evaluate(Object o1, Object o2) {
			double norm1 = getNorm(o1);
			double norm2 = getNorm(o2);
			if (norm1 != 0 && norm2 != 0)
				return baseKernel.evaluate(o1, o2) / (norm1 * norm2);
			else
				return 0;
		}

		@Override
		public String cacheKey(Object o) {
			return baseKernel.cacheKey(o);
		}
	}

	// public class AbstractRelation<P,C> implements Relation<P,C> {
	// private P parent;
	// private List<C> children;
	// private Kernel<C> kernel;
	// @Override
	// public List<C> getChildren() {
	// return children;
	// }
	// @Override
	// public P getParent() {
	// return parent;
	// }
	// @Override
	// public Kernel<C> getKernel() {
	// return kernel;
	// }
	// }
	// public class ReportDocumentRelation<Integer, Document> extends
	// AbstractRelation<Integer, Document> {
	// }
	// public class DocumentSentenceRelation<Document, AnnoBase> extends
	// AbstractRelation<Document, AnnoBase> {
	// }
	// public class SentenceNamedEntityRelation<AnnoBase, NamedEntityAnnotation>
	// extends AbstractRelation<AnnoBase, NamedEntityAnnotation> {
	// }

	public class Node {
		
		public String toString() {
			StringBuilder b = new StringBuilder();
			if(this.node instanceof Object[]) {
				for(Object o : (Object[])node) {
					b.append(o).append(" ");
				}
			} else if (this.node instanceof int[]) {
				for(int i : (int[])node) {
					b.append(i).append(" ");
				}
			} else {
				b.append(this.node).append(" ");
			}
			if(this.children.size() > 0) {
				b.append("(");
				for(Node n : children) {
					b.append(n);
				}
				b.append(")");
			}
			return b.toString();
		}
		
		public Kernel getKernel() {
			return kernel;
		}

		public void setKernel(Kernel kernel) {
			this.kernel = kernel;
		}

		public List<Node> getChildren() {
			return children;
		}

		public void setChildren(List<Node> children) {
			this.children = children;
		}

		public Object getNode() {
			return node;
		}

		public void setNode(Object node) {
			this.node = node;
		}

		Kernel kernel;
		Object node;
		List<Node> children = new ArrayList<Node>();

		public Node(Kernel kernel, Object node) {
			super();
			this.kernel = kernel;
			this.node = node;
		}

		public Node() {
		}
	}

//	private List<Object[]> getCMCDocuments(int instanceId) {
//		Query q = sessionFactory
//				.getCurrentSession()
//				.createQuery(
//						"select a.documentTypeID, a.document.documentID from DocumentKeyAnnotation a where a.uid = :uid");
//		q.setInteger("uid", instanceId);
//		return q.list();
//	}

//	private List<SentenceAnnotation> getSentences(int instanceId) {
//		Query q = sessionFactory
//				.getCurrentSession()
//				.createQuery(
//						"select s from SentenceAnnotation s, DocumentKeyAnnotation k where k.document.documentID = s.document.documentID and k.uid = :uid)");
//		q.setInteger("uid", instanceId);
//		return q.list();
//	}

//	private List<NamedEntityAnnotation> getNamedEntities(int instanceId) {
//		Query q = sessionFactory
//				.getCurrentSession()
//				.createQuery(
//						"select ne from NamedEntityAnnotation ne inner join fetch ne.ontologyConcepts, DocumentKeyAnnotation k where ne.document.documentID = k.document.documentID and k.uid = :uid)");
//		q.setInteger("uid", instanceId);
//		return q.list();
//	}

	/**
	 * SQL for comparison:
	 * select k.uid, k.document_type_id, k.document_id, s.anno_base_id, nea.anno_base_id, c.code
from v_dockey k
inner join v_annotation s on k.document_id = s.document_id and s.uima_type_name like '%sentence'
inner join v_annotation nea on nea.document_id = s.document_id and nea.span_begin >= s.span_begin and nea.span_end <= s.span_end and nea.uima_type_name like '%namedentity'
inner join anno_ontology_concept c on c.anno_base_id = nea.anno_base_id
where k.uid = 97634811
order by uid, document_type_id, document_id, s.anno_base_id, nea.anno_base_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Object[]> getAllConcepts() {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(
				"getAllConcepts");
		return q.list();
	}

	/**
	 * 
	 * @return
	 */
	public Map<Integer, Node> initializeObjectTrees() {
		List<Object[]> allConcepts = getAllConcepts();
		int currentInstanceID = 0;
		int currentDocumentID = 0;
		int currentSentenceID = 0;
		int currentNamedEntityID = 0;
		Node currentNamedEntity = null;
		Node currentSentence = null;
		Node currentDocument = null;
		Node currentInstance = null;
		Map<Integer, Node> instanceMap = new HashMap<Integer, Node>();
		for (Object[] concept : allConcepts) {
			int instanceID = (Integer) concept[0];
			int documentTypeID = (Integer) concept[1];
			int documentID = (Integer) concept[2];
			int sentenceID = (Integer) concept[3];
			int namedEntityID = (Integer) concept[4];
			float confidence = (Float) concept[5];
			String conceptID = (String) concept[6];
			if (instanceID != currentInstanceID) {
				// starting on new instance
				currentInstance = new Node(this.instanceKernel, instanceID);
				currentInstanceID = instanceID;
				instanceMap.put(instanceID, currentInstance);
			}
			if (documentID != currentDocumentID) {
				// starting on new document
				currentDocument = new Node(this.documentKernel, new Object[] {
						documentTypeID, documentID });
				currentDocumentID = documentID;
				currentInstance.getChildren().add(currentDocument);
			}
			if (sentenceID != currentSentenceID) {
				// starting on new sentence
				currentSentence = new Node(this.sentenceKernel,
						sentenceID);
				currentSentenceID = sentenceID;
				currentDocument.getChildren().add(currentSentence);
			}
			if (namedEntityID != currentNamedEntityID) {
				// starting on new named entity
				currentNamedEntity = new Node(namedEntityKernel, new Object[] {
						namedEntityID, confidence });
				currentNamedEntityID = namedEntityID;
				currentSentence.getChildren().add(currentNamedEntity);
			}
			// could have repeats of a concept for a single named entity, but
			// don't check this
			currentNamedEntity.getChildren().add(
					new Node(conceptKernel, conceptID));
		}
		return instanceMap;
	}

//	/**
//	 * Generate object tree for the given report. We will then apply the kernels
//	 * 
//	 * @param instanceId
//	 * @return
//	 */
//	public Node initializeObjectTree(int instanceId) {
//		List<Object[]> cmcDocuments = getCMCDocuments(instanceId);
//		List<SentenceAnnotation> sentences = getSentences(instanceId);
//		List<NamedEntityAnnotation> namedEntities = getNamedEntities(instanceId);
//		Node root = new Node();
//		root.setNode(instanceId);
//		root.setKernel(this.instanceKernel);
//		// add documents
//		for (Object[] documentSection : cmcDocuments) {
//			Document d = (Document) documentSection[1];
//			Node documentRel = new Node();
//			documentRel.setNode(documentSection);
//			documentRel.setKernel(documentKernel);
//			root.getChildren().add(documentRel);
//			// add sentences within each document
//			for (SentenceAnnotation sa : sentences) {
//				if (sa.getDocument().getDocumentID() == d.getDocumentID()) {
//					Node sentenceRel = new Node();
//					sentenceRel.setNode(sa);
//					sentenceRel.setKernel(sentenceKernel);
//					documentRel.getChildren().add(sentenceRel);
//					// add named entity within each sentence
//					for (NamedEntityAnnotation ne : namedEntities) {
//						if (ne.getDocument().getDocumentID() == d
//								.getDocumentID()
//								&& ne.getBegin() >= sa.getBegin()
//								&& ne.getEnd() <= sa.getEnd()) {
//							Node neRel = new Node();
//							neRel.setKernel(namedEntityKernel);
//							neRel.setNode(ne);
//							// add concept ids to named entities
//							for (OntologyConceptAnnotation oc : ne
//									.getOntologyConcepts()) {
//								Node neConcept = new Node();
//								neConcept.setKernel(conceptKernel);
//								neConcept.setNode(oc.getCode());
//								neRel.getChildren().add(neConcept);
//							}
//							sentenceRel.getChildren().add(neRel);
//						}
//					}
//				}
//			}
//		}
//		return root;
//	}

	public double calculateSimilarity(int instanceId1, int instanceId2) {
//		Node n1 = this.initializeObjectTree(instanceId1);
//		Node n2 = this.initializeObjectTree(instanceId2);
		Node n1 = this.instanceTreeMap.get(instanceId1);
		Node n2 = this.instanceTreeMap.get(instanceId2);
		if(log.isDebugEnabled()) {
			log.debug("n1: " + n1);
			log.debug("n2: " + n2);
		}
		return n1.getKernel().evaluate(n1, n2);
	}

	public void init() {
		normCache = cacheManager.getCache("normCache");
		conceptSimCache = cacheManager.getCache("conceptSimCache");
		TransactionTemplate t = new TransactionTemplate(this.transactionManager);
		t.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
		t.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				instanceTreeMap = initializeObjectTrees();
				return null;
			}
		});
	}
}
