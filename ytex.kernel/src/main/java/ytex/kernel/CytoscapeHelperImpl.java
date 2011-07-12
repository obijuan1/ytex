package ytex.kernel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.umls.dao.UMLSDao;

public class CytoscapeHelperImpl implements CytoscapeHelper {
	/**
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
		OptionGroup og = new OptionGroup();
		og.addOption(OptionBuilder
				.withArgName("prefix")
				.hasArg()
				.withDescription(
						"create network using specified concept graph and corpus. creates prefix.sif with edges and prefix.node.txt with node data in working directory.")
				.create("network"));
		og.addOption(OptionBuilder
				.withArgName("concept id")
				.hasArg()
				.withDescription(
						"get all descendants of specified concept, creates concept_id.tree file in working directory")
				.create("subtree"));
		og.setRequired(true);
		options.addOptionGroup(og);
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);
			CytoscapeHelper cytHelper = KernelContextHolder
					.getApplicationContext().getBean(CytoscapeHelper.class);
			Properties props = FileUtil.loadProperties(
					line.getOptionValue("prop"), true);
			if (!cytHelper.validateProps(props)) {
				printHelp(options);
			} else {
				if (line.hasOption("network")) {
					cytHelper.exportNetwork(line.getOptionValue("network"),
							props);
				} else if (line.hasOption("subtree")) {
					cytHelper.exportSubtree(line.getOptionValue("subtree"),
							props);
				} else {
					printHelp(options);
				}
			}
		} catch (ParseException pe) {
			printHelp(options);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + CytoscapeHelperImpl.class.getName()
				+ " generate graphs and node lists for cytoscape", options);
	}

	protected ClassifierEvaluationDao classifierEvaluationDao;

	protected ConceptDao conceptDao;

	protected UMLSDao umlsDao;

	private void addConcepts(ConceptGraph cg, String conceptId,
			Set<String> nodesToInclude, Set<String> leaves) {
		ConcRel cr = cg.getConceptMap().get(conceptId);
		// only process this node if it isn't already in the list
		if (!nodesToInclude.contains(cr.getConceptID())) {
			// add me to the list
			nodesToInclude.add(cr.getConceptID());
			// iterate over parents and recurse
			for (ConcRel crp : cr.getParents()) {
				addConcepts(cg, crp.getConceptID(), nodesToInclude, leaves);
				// parent is not a leaf - remove it from the list of candidate
				// leaves
				leaves.remove(crp.getConceptID());
			}
		}
	}

	private void addSubtree(Set<String> nodes, ConcRel cr) {
		if (!nodes.contains(cr.getConceptID())) {
			nodes.add(cr.getConceptID());
			for (ConcRel crc : cr.getChildren()) {
				addSubtree(nodes, crc);
			}
		}
	}

	private Set<String> exportEdges(ConceptGraph cg,
			Set<String> nodesToInclude, Set<String> leaves,
			int leafChildrenDepth, BufferedWriter network) throws IOException {
		Set<String> exportedNodes = new HashSet<String>();
		for (String conceptID : nodesToInclude) {
			ConcRel cr = cg.getConceptMap().get(conceptID);
			exportedNodes.add(cr.getConceptID());
			for (ConcRel crc : cr.getChildren()) {
				network.write(crc.getConceptID());
				network.write("\tisa\t");
				network.write(cr.getConceptID());
				network.write("\n");
				exportedNodes.add(crc.getConceptID());
			}
		}
		return exportedNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.CytoscapeHelper#exportNetwork(ytex.kernel.model.ConceptGraph,
	 * java.lang.String, java.lang.String, java.lang.String, int,
	 * java.io.BufferedWriter, java.io.BufferedWriter)
	 */
	@Override
	public void exportNetwork(ConceptGraph cg, String corpusName,
			String conceptGraphName, String conceptSetName,
			int leafChildrenDepth, BufferedWriter networkData,
			BufferedWriter nodeData) throws IOException {
		Map<String, Double> ic = this.classifierEvaluationDao
				.getFeatureRankEvaluations(corpusName, conceptSetName, null,
						CorpusEvaluator.INFOCONTENT, 0, conceptGraphName);
		Set<String> nodesToInclude = new HashSet<String>();
		Set<String> leaves = new HashSet<String>();
		// candidate leaves
		leaves.addAll(ic.keySet());
		for (String conceptId : ic.keySet()) {
			// add all paths to root for the concept
			addConcepts(cg, conceptId, nodesToInclude, leaves);
		}

		Set<String> exportedNodes = exportEdges(cg, nodesToInclude, leaves,
				leafChildrenDepth, networkData);
		exportNodes(exportedNodes, nodeData, ic);
	}

	@Override
	public void exportNetwork(String filePrefix, Properties props)
			throws IOException {
		BufferedWriter networkData = null;
		BufferedWriter nodeData = null;
		try {
			networkData = new BufferedWriter(
					new FileWriter(filePrefix + ".sif"));
			nodeData = new BufferedWriter(new FileWriter(filePrefix
					+ ".node.txt"));
			String conceptGraphName = props
					.getProperty("ytex.conceptGraphName");
			exportNetwork(this.conceptDao.getConceptGraph(conceptGraphName),
					props.getProperty("ytex.corpusName"), conceptGraphName,
					props.getProperty("ytex.conceptSetName"), 0, networkData,
					nodeData);
		} finally {
			if (networkData != null) {
				networkData.close();
			}
			if (nodeData != null) {
				nodeData.close();
			}
		}
	}

	private void exportNodes(List<String> subList, BufferedWriter nodeData,
			Map<String, Double> ic) throws IOException {
		Map<String, String> nodeNames = this.umlsDao.getNames(subList);
		for (String conceptID : subList) {
			nodeData.write(conceptID);
			nodeData.write("\t");
			nodeData.write(Double.toString(ic.containsKey(conceptID) ? ic
					.get(conceptID) : 0.0));
			nodeData.write("\t\"");
			nodeData.write(nodeNames.containsKey(conceptID) ? nodeNames.get(
					conceptID).toString() : "");
			nodeData.write("\"\n");
		}

	}

	private void exportNodes(Set<String> exportedNodes,
			BufferedWriter nodeData, Map<String, Double> ic) throws IOException {
		List<String> exportedNodeList = new ArrayList<String>(exportedNodes);
		int size = exportedNodes.size();
		int chunks = size / 1000;
		if (size % 1000 != 0)
			chunks++;
		for (int chunk = 0; chunk < chunks; chunk++) {
			int start = chunk * 1000;
			int end = Math.min(size - 1, (chunk + 1) * 1000 - 1);
			List<String> subList = exportedNodeList.subList(start, end);
			exportNodes(subList, nodeData, ic);
		}
	}

	@Override
	public void exportSubtree(String conceptID, Properties props)
			throws IOException {
		Set<String> nodes = new HashSet<String>();
		ConceptGraph cg = this.conceptDao.getConceptGraph(props
				.getProperty("ytex.conceptGraphName"));
		ConcRel cr = cg.getConceptMap().get(conceptID);
		if (cr != null) {
			addSubtree(nodes, cr);
		}
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(conceptID + ".idlist"));
			for (String node : nodes) {
				w.write(node);
				w.write("\n");
			}
		} finally {
			if (w != null)
				w.close();
		}

	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}


	public ConceptDao getConceptDao() {
		return conceptDao;
	}

	public UMLSDao getUmlsDao() {
		return umlsDao;
	}


	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public void setConceptDao(ConceptDao conceptDao) {
		this.conceptDao = conceptDao;
	}

	public void setUmlsDao(UMLSDao umlsDao) {
		this.umlsDao = umlsDao;
	}

	@Override
	public boolean validateProps(Properties props) {
		String corpusName = props.getProperty("ytex.corpusName");
		String conceptGraphName = props.getProperty("ytex.conceptGraphName");
		return corpusName != null && conceptGraphName != null;
	}
	
}
