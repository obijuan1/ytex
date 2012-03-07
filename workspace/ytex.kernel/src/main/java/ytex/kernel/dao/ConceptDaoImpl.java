package ytex.kernel.dao;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import ytex.kernel.FileUtil;
import ytex.kernel.IntrinsicInfoContentEvaluator;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;

public class ConceptDaoImpl implements ConceptDao {
	/**
	 * the default concept id for the root. override with -Dytex.defaultRootId
	 */
	private static final String DEFAULT_ROOT_ID = "C0000000";
	/**
	 * ignore forbidden concepts. list Taken from umls-interface.
	 * 	f concept is one of the following just return #C1274012|Ambiguous
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
	private static final String forbiddenConceptArr[] = new String[] {
			"C1274012", "C1274013", "C1276325", "C1274014", "C1274015",
			"C1274021", "C1443286", "C1274012", "C2733115" };
	private static Set<String> forbiddenConcepts;
	private static final Log log = LogFactory.getLog(ConceptDaoImpl.class);

	static {
		forbiddenConcepts = new HashSet<String>();
		forbiddenConcepts.addAll(Arrays.asList(forbiddenConceptArr));
	}

	/**
	 * create a concept graph. 1st param - name of concept graph. 2nd param -
	 * query to retrieve parent-child pairs.
	 * 
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String args[]) throws ParseException, IOException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("prop")
				.hasArg()
				.isRequired()
				.withDescription(
						"property file with queries and other parameters. todo desc")
				.create("prop"));
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);
			Properties props = FileUtil.loadProperties(
					line.getOptionValue("prop"), true);
			String conceptGraphName = props
					.getProperty("ytex.conceptGraphName");
			String conceptGraphQuery = props
					.getProperty("ytex.conceptGraphQuery");
			String strCheckCycle = props.getProperty("ytex.checkCycle", "true");
			boolean checkCycle = true;
			if ("false".equalsIgnoreCase(strCheckCycle)
					|| "no".equalsIgnoreCase(strCheckCycle))
				checkCycle = false;
			if (conceptGraphName != null && conceptGraphQuery != null) {
				KernelContextHolder
						.getApplicationContext()
						.getBean(ConceptDao.class)
						.createConceptGraph(conceptGraphName,
								conceptGraphQuery, checkCycle);
			} else {
				printHelp(options);
			}
		} catch (ParseException pe) {
			printHelp(options);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + ConceptDaoImpl.class.getName()
				+ " generate concept graph", options);
	}

	private IntrinsicInfoContentEvaluator intrinsicInfoContentEvaluator;

	private JdbcTemplate jdbcTemplate;

	private SessionFactory sessionFactory;

	private Properties ytexProperties;

	/**
	 * add the relationship to the concept map
	 * 
	 * @param conceptMap
	 * @param conceptIndexMap
	 * @param conceptList
	 * @param roots
	 * @param conceptPair
	 */
	private void addRelation(ConceptGraph cg, Set<String> roots,
			String childCUI, String parentCUI, boolean checkCycle) {
		if (forbiddenConcepts.contains(childCUI)
				|| forbiddenConcepts.contains(parentCUI)) {
			// ignore relationships to useless concepts
			return;
		}
		// ignore self relations
		if (!childCUI.equals(parentCUI)) {
			boolean parNull = false;
			// get parent from cui map
			ConcRel crPar = cg.getConceptMap().get(parentCUI);
			if (crPar == null) {
				parNull = true;
				// parent not in cui map - add it
				crPar = cg.addConcept(parentCUI);
				// this is a candidate root - add it to the set of roots
				roots.add(parentCUI);
			}
			// get the child cui
			ConcRel crChild = cg.getConceptMap().get(childCUI);
			// crPar already has crChild, return
			if (crChild != null && crPar.getChildren().contains(crChild))
				return;
			// avoid cycles - don't add child cui if it is an ancestor
			// of the parent. if the child is not yet in the map, then it can't
			// possibly induce a cycle.
			// if the parent is not yet in the map, it can't induce a cycle
			// else check for cycles
			// @TODO: this is very inefficient. implement feedback arc algo
			boolean bCycle = !parNull && crChild != null && checkCycle
					&& checkCycle(crPar, crChild);
			if (bCycle) {
				log.warn("skipping relation that induces cycle: par="
						+ parentCUI + ", child=" + childCUI);
			} else {
				if (crChild == null) {
					// child not in cui map - add it
					crChild = cg.addConcept(childCUI);
				} else {
					// remove the cui from the list of candidate roots
					if (roots.contains(childCUI))
						roots.remove(childCUI);
				}
				// link child to parent and vice-versa
				crPar.children.add(crChild);
				crChild.parents.add(crPar);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.dao.ConceptDao#createConceptGraph
	 */
	@Override
	public void createConceptGraph(String name, String query,
			final boolean checkCycle) throws IOException {
		ConceptGraph conceptGraph = getConceptGraph(name);
		if (conceptGraph != null) {
			if (log.isWarnEnabled())
				log.warn("createConceptGraph(): concept graph already exist, exiting");
		} else {
			if (log.isInfoEnabled())
				log.info("createConceptGraph(): file not found, initializing concept graph from database.");
			// final Map<String, ConcRel> conceptMap = new HashMap<String,
			// ConcRel>();
			// final List<String> conceptList = new ArrayList<String>();
			// final Map<String, Integer> conceptIndexMap = new HashMap<String,
			// Integer>();
			final ConceptGraph cg = new ConceptGraph();
			final Set<String> roots = new HashSet<String>();
			this.jdbcTemplate.query(query, new RowCallbackHandler() {
				int nRowsProcessed = 0;

				@Override
				public void processRow(ResultSet rs) throws SQLException {
					String child = rs.getString(1);
					String parent = rs.getString(2);
					addRelation(cg, roots, child, parent, checkCycle);
					nRowsProcessed++;
					if (nRowsProcessed % 10000 == 0) {
						log.info("processed " + nRowsProcessed + " edges");
					}
				}
			});
			// set the root
			// if there is only one potential root, use it
			// else use a synthetic root and add all the roots as its children
			String rootId = null;
			if (log.isDebugEnabled())
				log.debug("roots: " + roots);
			if (roots.size() == 1) {
				rootId = roots.iterator().next();
			} else {
				rootId = System.getProperty("ytex.defaultRootId",
						DEFAULT_ROOT_ID);
				ConcRel crRoot = cg.addConcept(rootId);
				for (String crChildId : roots) {
					ConcRel crChild = cg.getConceptMap().get(crChildId);
					crRoot.getChildren().add(crChild);
					crChild.getParents().add(crRoot);
				}
			}
			cg.setRoot(rootId);
			// // can't get the maximum depth unless we're sure there are no
			// cycles
			// if (checkCycle)
			// cg.setDepthMax(calculateDepthMax(rootId, cg.getConceptMap()));
			log.info("writing concept graph: " + name);
			writeConceptGraph(name, cg);
			writeConceptGraphProps(name, query, checkCycle);
			if (checkCycle) {
				log.info("computing intrinsic info for concept graph: " + name);
				this.intrinsicInfoContentEvaluator
						.evaluateIntrinsicInfoContent(name,
								getConceptGraphDir(), cg);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.dao.ConceptDao#getConceptGraph(java.util.Set)
	 */
	public ConceptGraph getConceptGraph(String name) {
		File f = new File(getConceptGraphFileName(name));
		if (log.isInfoEnabled())
			log.info("getConceptGraph(" + name
					+ ") initializing concept graph from file: " + f.getPath());
		if (f.exists()) {
			if (log.isInfoEnabled())
				log.info("getConceptGraph(" + name
						+ ") file exists, reading concept graph");
			return initializeConceptGraph(this.readConceptGraph(f));
		} else {
			return null;
		}
	}

	public String getConceptGraphDir() {
		return ytexProperties.getProperty("ytex.conceptGraphDir",
				System.getProperty("java.io.tmpdir"));
	}

	private String getConceptGraphFileName(String name) {
		return getConceptGraphDir() + File.separator + name + ".gz";
	}

	public DataSource getDataSource(DataSource ds) {
		return this.jdbcTemplate.getDataSource();
	}

	public IntrinsicInfoContentEvaluator getIntrinsicInfoContentEvaluator() {
		return intrinsicInfoContentEvaluator;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public Properties getYtexProperties() {
		return ytexProperties;
	}

	private boolean checkCycle(ConcRel crPar, ConcRel crChild) {
		TIntSet visitedNodes = new TIntHashSet();
		return hasAncestor(crPar, crChild, visitedNodes);
	}

	/**
	 * check cycle.
	 * 
	 * @param crPar
	 *            parent
	 * @param crChild
	 *            child that should not be an ancestor of parent
	 * @param visitedNodes
	 *            nodes we've visited in our search. keep track of this to avoid
	 *            visiting the same node multiple times
	 * @return true if crChild is an ancestor of crPar
	 */
	private boolean hasAncestor(ConcRel crPar, ConcRel crChild,
			TIntSet visitedNodes) {
		// see if we've already visited this node - if yes then no need to redo
		// this
		if (visitedNodes.contains(crPar.getNodeIndex()))
			return false;
		// see if we're the same
		if (crPar.getNodeIndex() == crChild.getNodeIndex())
			return true;
		// recurse
		for (ConcRel c : crPar.getParents()) {
			if (hasAncestor(c, crChild, visitedNodes))
				return true;
		}
		// add ourselves to the set of visited nodes so we no not to revisit
		// this
		visitedNodes.add(crPar.getNodeIndex());
		return false;
	}

	/**
	 * replace cui strings in concrel with references to other nodes. initialize
	 * the concept list
	 * 
	 * @param cg
	 * @return
	 */
	private ConceptGraph initializeConceptGraph(ConceptGraph cg) {
		Map<String, ConcRel> conceptMap = cg.getConceptMap();
		SortedMap<Integer, ConcRel> conceptIndexMap = new TreeMap<Integer, ConcRel>();
		for (ConcRel cr : conceptMap.values()) {
			cr.constructRel(conceptMap);
			conceptIndexMap.put(cr.getNodeIndex(), cr);
		}
		cg.setConceptList(new ArrayList<ConcRel>(conceptIndexMap.values()));
		return cg;
	}

	private ConceptGraph readConceptGraph(File file) {
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new BufferedInputStream(
					new GZIPInputStream(new FileInputStream(file))));
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
					e.printStackTrace();
				}
		}

	}

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
	}

	public void setIntrinsicInfoContentEvaluator(
			IntrinsicInfoContentEvaluator intrinsicInfoContentEvaluator) {
		this.intrinsicInfoContentEvaluator = intrinsicInfoContentEvaluator;
	}

	// /**
	// * get maximum depth of graph.
	// *
	// * @param roots
	// * @param conceptMap
	// * @return
	// */
	// private int calculateDepthMax(String rootId, Map<String, ConcRel>
	// conceptMap) {
	// ConcRel crRoot = conceptMap.get(rootId);
	// return crRoot.depthMax();
	// }

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setYtexProperties(Properties ytexProperties) {
		this.ytexProperties = ytexProperties;
	}

//	/**
//	 * add parent to all descendants of crChild
//	 * 
//	 * @param crPar
//	 * @param crChild
//	 * @param ancestorCache
//	 */
//	private void updateDescendants(Set<Integer> ancestorsPar, ConcRel crChild,
//			Map<Integer, Set<Integer>> ancestorCache, int depth) {
//		if (ancestorCache != null) {
//			Set<Integer> ancestors = ancestorCache.get(crChild.nodeIndex);
//			if (ancestors != null)
//				ancestors.addAll(ancestorsPar);
//			// recurse
//			for (ConcRel crD : crChild.getChildren()) {
//				updateDescendants(ancestorsPar, crD, ancestorCache, depth + 1);
//			}
//		}
//	}

	/**
	 * write the concept graph, create parent directories as required
	 * 
	 * @param name
	 * @param cg
	 */
	private void writeConceptGraph(String name, ConceptGraph cg) {
		ObjectOutputStream os = null;
		File cgFile = new File(getConceptGraphFileName(name));
		if (!cgFile.getParentFile().exists())
			cgFile.getParentFile().mkdirs();
		try {
			os = new ObjectOutputStream(new BufferedOutputStream(
					new GZIPOutputStream(new FileOutputStream(cgFile))));
			os.writeObject(cg);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void writeConceptGraphProps(String name, String query,
			boolean checkCycle) {
		File propFile = new File(FileUtil.addFilenameToDir(
				this.getConceptGraphDir(), name + ".xml"));
		try {
			if (!propFile.exists()) {
				Properties props = new Properties();
				props.put("ytex.conceptGraphQuery", query);
				props.put("ytex.conceptGraphName", name);
				props.put("ytex.checkCycle", checkCycle ? "true" : "false");
				OutputStream os = null;
				try {
					os = new FileOutputStream(propFile);
					props.storeToXML(os, "created on " + (new Date()));
				} finally {
					if (os != null) {
						try {
							os.close();
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
