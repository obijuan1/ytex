package ytex.kernel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.dao.ConceptDao;
import ytex.kernel.model.ConcRel;
import ytex.kernel.model.ConceptGraph;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureRank;

/**
 * Calculate the information content of each concept in a corpus wrt the
 * specified concept graph. Required properties:
 * <ul>
 * <li>ytex.conceptGraphName - required - name of conceptGraph. @see ConceptDao
 * <li>ytex.corpusName - required - name of corpus
 * <li>ytex.conceptSetName - optional - you may want to experiment with
 * different sets of concepts from a corpus, e.g. concepts from certain
 * sections, or different ways of counting concepts.
 * <li>ytex.freqQuery - query to obtain raw concept frequencies for the corpus
 * </ul>
 * to execute, either specify these options via system properties (-D options)
 * on the command line, or supply this class with the path to a properties file
 * used for evaluation, or both (-D overrides properties file).
 * <p>
 * The information content of each concept is stored in the feature_rank table.
 * The related record in the feature_eval table has 
 * <ul>
 * 	<li>type = infocontent
 * 	<li>feature_set_name = conceptSetName
 * 	<li>param1 = conceptGraphName
 * </ul>
 * 
 * @author vijay
 * 
 */
public class InfoContentEvaluatorImpl implements InfoContentEvaluator {
	private ConceptDao conceptDao;
	// private CorpusDao corpusDao;
	private JdbcTemplate jdbcTemplate;
	private ClassifierEvaluationDao classifierEvaluationDao;

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public ConceptDao getConceptDao() {
		return conceptDao;
	}

	public void setConceptDao(ConceptDao conceptDao) {
		this.conceptDao = conceptDao;
	}

	// public CorpusDao getCorpusDao() {
	// return corpusDao;
	// }
	//
	// public void setCorpusDao(CorpusDao corpusDao) {
	// this.corpusDao = corpusDao;
	// }

	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
	}

	public DataSource getDataSource(DataSource ds) {
		return this.jdbcTemplate.getDataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.kernel.CorpusEvaluator#evaluateCorpusInfoContent(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void evaluateCorpusInfoContent(String freqQuery, String corpusName,
			String conceptGraphName, String conceptSetName) {
		ConceptGraph cg = this.conceptDao.getConceptGraph(conceptGraphName);
		classifierEvaluationDao.deleteFeatureEvaluation(corpusName,
				conceptSetName, null, INFOCONTENT, 0, conceptGraphName);
		FeatureEvaluation eval = new FeatureEvaluation();
		eval.setCorpusName(corpusName);
		if (conceptSetName != null)
			eval.setFeatureSetName(conceptSetName);
		eval.setEvaluationType(INFOCONTENT);
		eval.setParam1(conceptGraphName);
		// CorpusEvaluation eval = corpusDao.getCorpus(corpusName,
		// conceptGraphName, conceptSetName);
		// if (eval == null) {
		// eval = new CorpusEvaluation();
		// eval.setConceptGraphName(conceptGraphName);
		// eval.setConceptSetName(conceptSetName);
		// eval.setCorpusName(corpusName);
		// this.corpusDao.addCorpus(eval);
		// }
		Map<String, Double> rawFreq = getFrequencies(freqQuery);
		double totalFreq = 0d;
		// map of cui to cumulative frequency
		Map<String, Double> conceptFreq = new HashMap<String, Double>(cg
				.getConceptMap().size());
		// recurse through the tree
		for (String conceptId : cg.getRoots()) {
			totalFreq += getFrequency(cg.getConceptMap().get(conceptId),
					conceptFreq, rawFreq);
		}
		List<FeatureRank> featureRankList = new ArrayList<FeatureRank>(
				conceptFreq.size());
		// update information content
		double log2inv = -1d / Math.log(2);
		for (Map.Entry<String, Double> cfreq : conceptFreq.entrySet()) {
			// ConceptInformationContent ic = new ConceptInformationContent();
			if (cfreq.getValue() > 0) {
				FeatureRank featureRank = new FeatureRank(eval, cfreq.getKey(),
						log2inv * Math.log(cfreq.getValue() / totalFreq));
				featureRankList.add(featureRank);
				// ic.setCorpus(eval);
				// ic.setConceptId(cfreq.getKey());
				// ic.setFrequency(cfreq.getValue());
				// ic.setInformationContent(-Math.log(cfreq.getValue() /
				// totalFreq));
				// corpusDao.addInfoContent(ic);
			}
		}
		// the rank is irrelevant, but rank the features anyways
		eval.setFeatures(FeatureRank.sortFeatureRankList(featureRankList,
				new FeatureRank.FeatureRankDesc()));
		this.classifierEvaluationDao.saveFeatureEvaluation(eval);
	}

	/**
	 * get the frequency of each term in the corpus.
	 * 
	 * @param freqQuery
	 *            query returns 2 columns. 1st column - concept id (string), 2nd
	 *            column - frequency (double)
	 * @return
	 */
	public Map<String, Double> getFrequencies(String freqQuery) {
		// get the raw frequency
		final Map<String, Double> rawFreq = new HashMap<String, Double>();
		jdbcTemplate.query(freqQuery, new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				rawFreq.put(rs.getString(1), rs.getDouble(2));
			}
		});
		return rawFreq;
	}

	/**
	 * recursively sum frequency of parent and all its childrens' frequencies
	 * 
	 * @param parent
	 *            parent node
	 * @param conceptFreq
	 *            results stored here
	 * @param conceptIdToTermMap
	 *            raw frequencies here
	 * @return double sum of concept frequency in the subtree with parent as
	 *         root
	 */
	double getFrequency(ConcRel parent, Map<String, Double> conceptFreq,
			Map<String, Double> rawFreq) {
		double dFreq = 0d;
		if (conceptFreq.containsKey(parent.getConceptID())) {
			dFreq = conceptFreq.get(parent.getConceptID());
		} else {
			// get raw freq
			dFreq = rawFreq.containsKey(parent.getConceptID()) ? rawFreq
					.get(parent.getConceptID()) : 0d;
			// recurse
			for (ConcRel child : parent.children) {
				dFreq += getFrequency(child, conceptFreq, rawFreq);
			}
			conceptFreq.put(parent.getConceptID(), dFreq);
		}
		return dFreq;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption(OptionBuilder
				.withArgName("property file")
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
			if (!props.containsKey("ytex.conceptGraphName")
					|| !props.containsKey("ytex.corpusName")
					|| !props.containsKey("ytex.freqQuery")) {
				System.err.println("error: required parameter not specified");
				System.exit(1);
			} else {
				InfoContentEvaluator corpusEvaluator = KernelContextHolder
						.getApplicationContext().getBean(InfoContentEvaluator.class);
				corpusEvaluator.evaluateCorpusInfoContent(
						props.getProperty("ytex.freqQuery"),
						props.getProperty("ytex.corpusName"),
						props.getProperty("ytex.conceptGraphName"),
						props.getProperty("ytex.conceptSetName"));
				System.exit(0);
			}
		} catch (ParseException pe) {
			printHelp(options);
			System.exit(1);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + InfoContentEvaluatorImpl.class.getName()
				+ " calculate information content of corpus wrt concept graph",
				options);
	}
}
