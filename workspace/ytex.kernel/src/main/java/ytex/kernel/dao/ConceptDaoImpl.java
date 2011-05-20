package ytex.kernel.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;

import ytex.kernel.KernelContextHolder;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.umls.dao.UMLSDao;

public class ConceptDaoImpl implements ConceptDao {
	private SessionFactory sessionFactory;
	private static final Log log = LogFactory.getLog(ConceptDaoImpl.class);
	private UMLSDao umlsDao;
	private Properties ytexProperties;

	public Properties getYtexProperties() {
		return ytexProperties;
	}

	public void setYtexProperties(Properties ytexProperties) {
		this.ytexProperties = ytexProperties;
	}

	/**
	 * ignore forbidden concepts. list Taken from umls-interface
	 */
	private static final String forbiddenConceptArr[] = new String[] {
			"C1274012", "C1274013", "C1276325", "C1274014", "C1274015",
			"C1274021", "C1443286", "C1274012", "C2733115" };
	private static Set<String> forbiddenConcepts;
	static {
		forbiddenConcepts = new HashSet<String>();
		forbiddenConcepts.addAll(Arrays.asList(forbiddenConceptArr));
	}

	/*
	 * # if concept is one of the following just return #C1274012|Ambiguous
	 * concept (inactive concept) if($concept=~/C1274012/) { return 1; }
	 * #C1274013|Duplicate concept (inactive concept) if($concept=~/C1274013/) {
	 * return 1; } #C1276325|Reason not stated concept (inactive concept)
	 * if($concept=~/C1276325/) { return 1; } #C1274014|Outdated concept
	 * (inactive concept) if($concept=~/C1274014/) { return 1; }
	 * #C1274015|Erroneous concept (inactive concept) if($concept=~/C1274015/) {
	 * return 1; } #C1274021|Moved elsewhere (inactive concept)
	 * if($concept=~/C1274021/) { return 1; } #C1443286|unapproved attribute
	 * if($concept=~/C1443286/) { return 1; } #C1274012|non-current concept -
	 * ambiguous if($concept=~/C1274012/) { return 1; } #C2733115|limited status
	 * concept if($concept=~/C2733115/) { return 1; }
	 */

	public String getConceptGraphDir() {
		return ytexProperties.getProperty("ytex.conceptGraphDir",
				System.getProperty("java.io.tmpdir"));
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public UMLSDao getUmlsDao() {
		return umlsDao;
	}

	public void setUmlsDao(UMLSDao umlsDao) {
		this.umlsDao = umlsDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.dao.ConceptDao#initializeConceptGraph(java.util.Set)
	 */
	public ConceptGraph initializeConceptGraph(String sourceVocabularies[]) {
		ConceptGraph conceptGraph = getConceptGraph(sourceVocabularies);
		if (conceptGraph != null) {
			return conceptGraph;
		} else {
			if (log.isInfoEnabled())
				log.info("initializeConceptGraph(): file not found, initializing concept graph from database.");
			Map<String, ConcRel> conceptMap = new HashMap<String, ConcRel>();
			Set<String> roots = new HashSet<String>();
			List<Object[]> conceptPairs = sourceVocabularies.length == 0 ? umlsDao
					.getAllRelations() : umlsDao
					.getRelationsForSABs(sourceVocabularies);
			for (Object[] conceptPair : conceptPairs) {
				addRelation(conceptMap, roots, conceptPair);
			}
			ConceptGraph cg = new ConceptGraph();
			cg.setRoots(roots);
			cg.setSourceVocabularies(sourceVocabularies);
			cg.setConceptMap(conceptMap);
			cg.setDepthMax(calculateDepthMax(roots, conceptMap));
			writeConceptGraph(cg);
			// sessionFactory.getCurrentSession().save(cg);
			return cg;
		}
	}

	private void writeConceptGraph(ConceptGraph cg) {
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(this.getConceptGraphDir()
							+ "/conceptGraph")));
			os.writeObject(cg);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private ConceptGraph readConceptGraph(File file) {
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			return (ConceptGraph) is.readObject();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	/**
	 * add the relationship to the concept map
	 * 
	 * @param conceptMap
	 * @param roots
	 * @param conceptPair
	 */
	private void addRelation(Map<String, ConcRel> conceptMap,
			Set<String> roots, Object[] conceptPair) {
		String childCUI = (String) conceptPair[0];
		String parentCUI = (String) conceptPair[1];
		if (forbiddenConcepts.contains(childCUI)
				|| forbiddenConcepts.contains(parentCUI)) {
			// ignore relationships to useless concepts
			return;
		}
		// ignore self relations
		if (!childCUI.equals(parentCUI)) {
			// get parent from cui map
			ConcRel crPar = conceptMap.get(parentCUI);
			if (crPar == null) {
				// parent not in cui map - add it
				crPar = new ConcRel(parentCUI);
				conceptMap.put(parentCUI, crPar);
				// this is a candidate root - add it to the set of roots
				roots.add(parentCUI);
			}
			// avoid cycles - don't add child cui if it is an ancestor
			// of the parent
			if (!crPar.hasAncestor(childCUI)) {
				// get the child cui
				ConcRel crChild = conceptMap.get(childCUI);
				if (crChild == null) {
					// child not in cui map - add it
					crChild = new ConcRel(childCUI);
					conceptMap.put(childCUI, crChild);
				}
				// remove the cui from the list of candidate roots
				if (roots.contains(childCUI))
					roots.remove(childCUI);
				// link child to parent and vice-versa
				crPar.children.add(crChild);
				crChild.parents.add(crPar);
			}
		}
	}

	/**
	 * get maximum depth of graph.
	 * 
	 * @param roots
	 * @param conceptMap
	 * @return
	 */
	private int calculateDepthMax(Set<String> roots,
			Map<String, ConcRel> conceptMap) {
		int d = 0;
		// iterate through the roots
		for (String rootCUI : roots) {
			ConcRel r = conceptMap.get(rootCUI);
			// recursively compute the maximum depth
			int dm = r.depthMax() + 1;
			// if the depth from this root is greater, save it
			if (dm > d)
				d = dm;
		}
		return d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.dao.ConceptDao#getConceptGraph(java.util.Set)
	 */
	public ConceptGraph getConceptGraph(String sourceVocabularies[]) {
		// Query q = this.getSessionFactory().getCurrentSession().getNamedQuery(
		// "getConceptGraph");
		// q.setParameter("sourceVocabularies", sourceVocabularies);
		// ConceptGraph cg = (ConceptGraph) q.uniqueResult();
		// if (cg != null) {
		// initializeConceptGraph(cg);
		// }
		File f = new File(getConceptGraphDir() + "/conceptGraph");
		if (log.isInfoEnabled())
			log.info("getConceptGraph() initializing concept graph from file: "
					+ f.getPath());
		if (f.exists()) {
			if (log.isInfoEnabled())
				log.info("getConceptGraph() file exists, reading concept graph");
			return initializeConceptGraph(this.readConceptGraph(f));
		} else {
			return null;
		}
	}

	private ConceptGraph initializeConceptGraph(ConceptGraph cg) {
		Map<String, ConcRel> conceptMap = cg.getConceptMap();
		for (ConcRel cr : conceptMap.values()) {
			cr.constructRel(conceptMap);
		}
		return cg;
	}

	public static void main(String args[]) {
		KernelContextHolder.getApplicationContext().getBean(ConceptDao.class)
				.initializeConceptGraph(args);
	}

}
