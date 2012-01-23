package ytex.kernel.dao;

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
import ytex.kernel.KernelContextHolder;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;

public class ConceptDaoImpl implements ConceptDao {
	private SessionFactory sessionFactory;
	private static final Log log = LogFactory.getLog(ConceptDaoImpl.class);
	private JdbcTemplate jdbcTemplate;
	private Properties ytexProperties;
	/**
	 * the default concept id for the root. override with -Dytex.defaultRootId
	 */
	private static final String DEFAULT_ROOT_ID = "C0000000";

	public Properties getYtexProperties() {
		return ytexProperties;
	}

	public void setYtexProperties(Properties ytexProperties) {
		this.ytexProperties = ytexProperties;
	}

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
	}

	public DataSource getDataSource(DataSource ds) {
		return this.jdbcTemplate.getDataSource();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.kernel.dao.ConceptDao#createConceptGraph
	 */
	public ConceptGraph createConceptGraph(String name, String query) {
		ConceptGraph conceptGraph = getConceptGraph(name);
		if (conceptGraph != null) {
			if (log.isWarnEnabled())
				log.warn("createConceptGraph(): concept graph already exists, returning concept graph");
			return conceptGraph;
		} else {
			if (log.isInfoEnabled())
				log.info("createConceptGraph(): file not found, initializing concept graph from database.");
			final Map<String, ConcRel> conceptMap = new HashMap<String, ConcRel>();
			final Set<String> roots = new HashSet<String>();
			this.jdbcTemplate.query(query, new RowCallbackHandler() {
				int nRowsProcessed = 0;

				@Override
				public void processRow(ResultSet rs) throws SQLException {
					String child = rs.getString(1);
					String parent = rs.getString(2);
					addRelation(conceptMap, roots, child, parent);
					nRowsProcessed++;
					if (nRowsProcessed % 10000 == 0) {
						log.debug("processed " + nRowsProcessed + " edges");
					}
				}
			});
			ConceptGraph cg = new ConceptGraph();
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
				ConcRel crRoot = new ConcRel(rootId);
				for (String crChildId : roots) {
					ConcRel crChild = conceptMap.get(crChildId);
					crRoot.getChildren().add(crChild);
					crChild.getParents().add(crRoot);
				}
				conceptMap.put(rootId, crRoot);
			}
			cg.setRoot(rootId);
			cg.setConceptMap(conceptMap);
			cg.setDepthMax(calculateDepthMax(rootId, conceptMap));
			writeConceptGraph(name, cg);
			writeConceptGraphProps(name, query);
			return cg;
		}
	}

	private void writeConceptGraphProps(String name, String query) {
		File propFile = new File(FileUtil.addFilenameToDir(
				this.getConceptGraphDir(), name + ".xml"));
		try {
			if (!propFile.exists()) {
				Properties props = new Properties();
				props.put("ytex.conceptGraphQuery", query);
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

	/**
	 * add the relationship to the concept map
	 * 
	 * @param conceptMap
	 * @param roots
	 * @param conceptPair
	 */
	private void addRelation(Map<String, ConcRel> conceptMap,
			Set<String> roots, String childCUI, String parentCUI) {
		if (forbiddenConcepts.contains(childCUI)
				|| forbiddenConcepts.contains(parentCUI)) {
			// ignore relationships to useless concepts
			return;
		}
		// ignore self relations
		if (!childCUI.equals(parentCUI)) {
			boolean checkCycle = true;
			// get parent from cui map
			ConcRel crPar = conceptMap.get(parentCUI);
			if (crPar == null) {
				checkCycle = false;
				// parent not in cui map - add it
				crPar = new ConcRel(parentCUI);
				conceptMap.put(parentCUI, crPar);
				// this is a candidate root - add it to the set of roots
				roots.add(parentCUI);
			}
			// get the child cui
			ConcRel crChild = conceptMap.get(childCUI);
			// avoid cycles - don't add child cui if it is an ancestor
			// of the parent. if the child is not yet in the map, then it can't
			// possibly induce a cycle.
			// if the parent is not yet in the map, it can't induce a cycle
			// else check for cycles
			// @TODO: this is very inefficient. implement feedback arc algo
			boolean bCycle = crChild != null && checkCycle
					&& crPar.hasAncestor(childCUI);
			if (bCycle) {
				log.warn("skipping relation that induces cycle: par="
						+ parentCUI + ", child=" + childCUI);
			} else {
				if (crChild == null) {
					// child not in cui map - add it
					crChild = new ConcRel(childCUI);
					conceptMap.put(childCUI, crChild);
					checkCycle = false;
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

	/**
	 * get maximum depth of graph.
	 * 
	 * @param roots
	 * @param conceptMap
	 * @return
	 */
	private int calculateDepthMax(String rootId, Map<String, ConcRel> conceptMap) {
		ConcRel crRoot = conceptMap.get(rootId);
		return crRoot.depthMax();
	}

	private String getConceptGraphFileName(String name) {
		return getConceptGraphDir() + File.separator + name + ".gz";
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

	/**
	 * replace cui strings in concrel with references to other nodes.
	 * 
	 * @param cg
	 * @return
	 */
	private ConceptGraph initializeConceptGraph(ConceptGraph cg) {
		Map<String, ConcRel> conceptMap = cg.getConceptMap();
		for (ConcRel cr : conceptMap.values()) {
			cr.constructRel(conceptMap);
		}
		return cg;
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
			if (conceptGraphName != null && conceptGraphQuery != null) {
				KernelContextHolder
						.getApplicationContext()
						.getBean(ConceptDao.class)
						.createConceptGraph(conceptGraphName, conceptGraphQuery);
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

}
