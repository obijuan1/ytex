package ytex.cmc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import model.SujConcept;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.ConceptSimilarityService;

public class SujKernelImpl implements CMCKernel {
	private static final Log log = LogFactory.getLog(SujKernelImpl.class);
	SessionFactory sessionFactory;
	CacheManager cacheManager;
	Cache normCache;
	Cache conceptSimCache;
	ConceptSimilarityService conceptSimilarityService;
	PlatformTransactionManager transactionManager;
	SimpleJdbcTemplate simpleJdbcTemplate;
	DataSource dataSource;

	Map<String, Set<String>> cuiTuiMap;
	Map<String, Set<Integer>> cuiMainSuiMap;

	public static int getMainSem(int sui) {
		switch (sui) {
		case 52:
		case 53:
		case 56:
		case 51:
		case 64:
		case 55:
		case 66:
		case 57:
		case 54:
			return 0;
		case 17:
		case 29:
		case 23:
		case 30:
		case 31:
		case 22:
		case 25:
		case 26:
		case 18:
		case 21:
		case 24:
			return 1;
		case 116:
		case 195:
		case 123:
		case 122:
		case 118:
		case 103:
		case 120:
		case 104:
		case 200:
		case 111:
		case 196:
		case 126:
		case 131:
		case 125:
		case 129:
		case 130:
		case 197:
		case 119:
		case 124:
		case 114:
		case 109:
		case 115:
		case 121:
		case 192:
		case 110:
		case 127:
			return 2;
		case 185:
		case 77:
		case 169:
		case 102:
		case 78:
		case 170:
		case 171:
		case 80:
		case 81:
		case 89:
		case 82:
		case 79:
			return 3;
		case 203:
		case 74:
		case 75:
			return 4;
		case 20:
		case 190:
		case 49:
		case 19:
		case 47:
		case 50:
		case 33:
		case 37:
		case 48:
		case 191:
		case 46:
		case 184:
			return 5;
		case 87:
		case 88:
		case 28:
		case 85:
		case 86:
			return 6;
		case 83:
			return 7;
		case 100:
		case 3:
		case 11:
		case 8:
		case 194:
		case 7:
		case 12:
		case 99:
		case 13:
		case 4:
		case 96:
		case 16:
		case 9:
		case 15:
		case 1:
		case 101:
		case 2:
		case 98:
		case 97:
		case 14:
		case 6:
		case 10:
		case 204: // vng missing sui
		case 5:
			return 8;
		case 71:
		case 168:
		case 73:
		case 72:
		case 167:
			return 9;
		case 91:
		case 90:
			return 10;
		case 93:
		case 92:
		case 94:
		case 95:
			return 11;
		case 38:
		case 69:
		case 68:
		case 34:
		case 70:
		case 67:
			return 12;
		case 43:
		case 201:
		case 45:
		case 41:
		case 44:
		case 42:
		case 32:
		case 40:
		case 39:
			return 13;
		case 60:
		case 65:
		case 58:
		case 59:
		case 63:
		case 62:
		case 61:
			return 14;
		default:
			break;
		}
		return -1;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	Map<Integer, Node> instanceTreeMap;

	Kernel instanceKernel = new NormKernel(new ConvolutionKernel(
			new CMCInstanceKeyGenerator(), 2.0));
	Kernel documentKernel = new NormKernel(new DocumentKernel(
			new CMCDocumentKeyGenerator()));
	Kernel lexicalUnitKernel = new NormKernel(new ConvolutionKernel(
			new DefaultKeyGenerator()));
	Kernel normTermKernel = new NormKernel(new NormTermKernel());
	Kernel conceptKernel = new ConceptKernel();

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

	@Override
	public double calculateSimilarity(int instanceId1, int instanceId2) {
		// TODO Auto-generated method stub
		Node n1 = this.instanceTreeMap.get(instanceId1);
		Node n2 = this.instanceTreeMap.get(instanceId2);
		if (log.isDebugEnabled()) {
			log.debug("n1: " + n1);
			log.debug("n2: " + n2);
		}
		return n1.getKernel().evaluate(n1, n2);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getAllNormTerms() {
		String query = "select uid, document_type_id, ab.document_id, lu.lexical_unit_id, nt.norm_term_id, nt.normTerm "
				+ "from anno_base ab "
				+ "inner join anno_dockey k on ab.anno_base_id = k.anno_base_id "
				+ "inner join suj_lexical_unit lu on ab.document_id = lu.document_id "
				+ "inner join suj_norm_term nt on lu.lexical_unit_id = nt.lexical_unit_id "
				// + "where uid in (97634811, 97636670) "
				+ "order by uid, document_type_id, ab.document_id, lu.lexical_unit_id, nt.normTerm ";
		return simpleJdbcTemplate.queryForList(query);
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
				initCuiTuiMap();
				return null;
			}
		});
	}

	/**
	 * select k.uid, k.documentTypeID, k.document.documentID,
	 * lu.lexical_unit_id, nt.norm_term_id, nt.normTerm, c.cui
	 * 
	 * @return
	 */
	public Map<Integer, Node> initializeObjectTrees() {
		List<Map<String, Object>> allConcepts = getAllNormTerms();
		int currentInstanceID = 0;
		int currentDocumentID = 0;
		int currentLuID = 0;
		int currentNormTermID = 0;
		Node currentNormTerm = null;
		Node currentLexicalUnit = null;
		Node currentDocument = null;
		Node currentInstance = null;
		Map<Integer, Node> instanceMap = new HashMap<Integer, Node>();
		Map<Integer, Node> normTermMap = new HashMap<Integer, Node>();
		// fill in tree up to norm term
		for (Map<String, Object> concept : allConcepts) {
			// int instanceID = (Integer) concept[0];
			// int documentTypeID = (Integer) concept[1];
			// int documentID = (Integer) concept[2];
			// int luID = (Integer) concept[3];
			// int normTermID = (Integer) concept[4];
			// String normTerm = (String) concept[5];
			int instanceID = (Integer) concept.get("uid");
			int documentTypeID = (Integer) concept.get("document_type_id");
			int documentID = (Integer) concept.get("document_id");
			int luID = (Integer) concept.get("lexical_unit_id");
			int normTermID = (Integer) concept.get("norm_term_id");
			String normTerm = (String) concept.get("normTerm");
			// String conceptID = (String) concept[6];
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
			if (luID != currentLuID) {
				// starting on new lu
				currentLexicalUnit = new Node(this.lexicalUnitKernel, luID);
				currentLuID = luID;
				currentDocument.getChildren().add(currentLexicalUnit);
			}
			if (normTermID != currentNormTermID) {
				// starting on new norm term
				currentNormTerm = new Node(this.normTermKernel, new Object[] {
						normTermID, normTerm });
				currentNormTermID = normTermID;
				currentLexicalUnit.getChildren().add(currentNormTerm);
				normTermMap.put(normTermID, currentNormTerm);
			}
		}
		// fill in concepts
		List<SujConcept> concepts = sessionFactory.getCurrentSession()
				.getNamedQuery("suj.getAllConcepts").list();
		for (SujConcept c : concepts) {
			Node n = normTermMap.get(c.getNorm_term_id());
			if (n != null) {
				n.getChildren().add(new Node(this.conceptKernel, c.getCui()));
			}
		}
		return instanceMap;
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

	public interface Kernel {
		double evaluate(Object c1, Object c2);

		String cacheKey(Object c);
	}

	public class ConvolutionKernel implements Kernel {
		private KeyGenerator keyGenerator;
		private double pow = 1;

		public double evaluate(Object c1, Object c2) {
			Node n1 = (Node) c1;
			Node n2 = (Node) c2;
			double d = 0;
			for (Node child1 : n1.getChildren()) {
				for (Node child2 : n2.getChildren()) {
					d += child1.getKernel().evaluate(child1, child2);
				}
			}
			if (pow > 1)
				return Math.pow(d, pow);
			else
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

		public ConvolutionKernel(KeyGenerator keyGenerator, double pow) {
			this.keyGenerator = keyGenerator;
			this.pow = pow;
		}

		public ConvolutionKernel() {
		}
	}

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

	public class Node {

		public String toString() {
			StringBuilder b = new StringBuilder();
			if (this.node instanceof Object[]) {
				for (Object o : (Object[]) node) {
					b.append(o).append(" ");
				}
			} else if (this.node instanceof int[]) {
				for (int i : (int[]) node) {
					b.append(i).append(" ");
				}
			} else {
				b.append(this.node).append(" ");
			}
			if (this.children.size() > 0) {
				b.append("(");
				for (Node n : children) {
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

	// if(concepts.isEmpty()||t1.concepts.isEmpty())
	// {
	// if(normTerm.equalsIgnoreCase(t1.normTerm))
	// return 1.0;
	// else
	// return 0.0;
	// }
	//
	// for(ConceptUplet cu: concepts)
	// {
	// for(ConceptUplet cu2 : t1.concepts)
	// sim+=cu.calculateLinKernel(cu2);
	// }
	//
	// sim=sim/(norm*t1.norm);
	public class NormTermKernel extends ConvolutionKernel {
		String getNormTerm(Node n) {
			Object[] val = (Object[]) n.getNode();
			return (String) (val[1]);
		}

		@Override
		public double evaluate(Object c1, Object c2) {
			Node n1 = (Node) c1;
			Node n2 = (Node) c2;
			if (n1.getChildren().isEmpty() && n2.getChildren().isEmpty()) {
				return getNormTerm(n1).equals(getNormTerm(n2)) ? 1 : 0;
			} else {
				return super.evaluate(c1, c2);
			}
		}

		@Override
		public String cacheKey(Object c) {
			int id = (Integer) ((Object[]) ((Node) c).getNode())[0];
			return new StringBuilder("nt-").append(id).toString();
		}

	}

	// for (Integer o : mainSemUI) {
	// if ((flag = cu2.mainSemUI.contains(o)))
	// break;
	// }
	// if (!flag)
	// return 0.0;

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
			double d = 0;
			String c1 = (String) ((Node) o1).getNode();
			String c2 = (String) ((Node) o2).getNode();
			if (c1.equals(c2)) {
				d = 1;
			} else {
//				Set<String> tuis1 = cuiTuiMap.get(c1);
//				Set<String> tuis2 = cuiTuiMap.get(c2);
				Set<Integer> tuis1 = cuiMainSuiMap.get(c1);
				Set<Integer> tuis2 = cuiMainSuiMap.get(c2);
				// only compare the two if they have a common semantic type
				if (tuis1 != null && tuis2 != null
						&& !Collections.disjoint(tuis1, tuis2)) {
					// look in cache
					String key = createKey(c1, c2);
					Element e = conceptSimCache.get(key);
					if (e != null) {
						// it's there
						d = (Double) e.getObjectValue();
					} else {
						// it's not there - put it there
						d = conceptSimilarityService.lch(c1, c2)
								* conceptSimilarityService.lin("cmc-suj", c1,
										c2);
						conceptSimCache.put(new Element(key, d));
					}
				}
			}
			return d;
		}

		@Override
		public String cacheKey(Object o) {
			return null;
		}

	}

	public Set<Integer> tuiToMainSui(Set<String> tuis) {
		Set<Integer> mainSui = new HashSet<Integer>(tuis.size());
		for(String tui : tuis) {
			mainSui.add(getMainSem(Integer.parseInt(tui.substring(1))));
		}
		return mainSui;
	}
	
	public void initCuiTuiMap() {
		String query = "select m.cui, m.tui from umls.MRSTY m inner join (select distinct cui from suj_concept)s  on s.cui = m.cui";
		List<Map<String, Object>> results = simpleJdbcTemplate
				.queryForList(query);
		this.cuiTuiMap = new HashMap<String, Set<String>>();
		for (Map<String, Object> result : results) {
			String cui = (String) result.get("cui");
			String tui = (String) result.get("tui");
			Set<String> tuis = cuiTuiMap.get(cui);
			if (tuis == null) {
				tuis = new HashSet<String>();
				cuiTuiMap.put(cui, tuis);
			}
			tuis.add(tui);
		}
		this.cuiMainSuiMap = new HashMap<String, Set<Integer>>(cuiTuiMap.size());
		for(Map.Entry<String, Set<String>> cuiTui : cuiTuiMap.entrySet()) {
			cuiMainSuiMap.put(cuiTui.getKey(), tuiToMainSui(cuiTui.getValue()));			
		}
	}

}
