package ytex.weka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeSelection;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import ytex.kernel.FileUtil;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.SparseData;
import ytex.kernel.SparseDataExporter;
import ytex.kernel.dao.ClassifierEvaluationDao;
import ytex.kernel.model.ClassifierEvaluation;
import ytex.kernel.model.CrossValidationFold;
import ytex.kernel.model.FeatureEvaluation;
import ytex.kernel.model.FeatureRank;
import ytex.weka.WekaFormatterFactory.WekaFormatter;

public class WekaAttributeEvaluatorImpl implements WekaAttributeEvaluator {
	private static final Log log = LogFactory
			.getLog(WekaAttributeEvaluatorImpl.class);
	private ClassifierEvaluationDao classifierEvaluationDao;
	private ASEvaluation asEvaluation;
	private AttributeSelection attributeSelection;
	private SparseDataExporter sparseDataExporter;

	public SparseDataExporter getSparseDataExporter() {
		return sparseDataExporter;
	}

	public void setSparseDataExporter(SparseDataExporter sparseDataExporter) {
		this.sparseDataExporter = sparseDataExporter;
	}

	public ClassifierEvaluationDao getClassifierEvaluationDao() {
		return classifierEvaluationDao;
	}

	public void setClassifierEvaluationDao(
			ClassifierEvaluationDao classifierEvaluationDao) {
		this.classifierEvaluationDao = classifierEvaluationDao;
	}

	public ASEvaluation getAsEvaluation() {
		return asEvaluation;
	}

	public void setAsEvaluation(ASEvaluation asEvaluation) {
		this.asEvaluation = asEvaluation;
	}

	public AttributeSelection getAttributeSelection() {
		return attributeSelection;
	}

	public void setAttributeSelection(AttributeSelection attributeSelection) {
		this.attributeSelection = attributeSelection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.weka.WekaAttributeEvaluator#evaluateAttributesFromFile(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void evaluateAttributesFromFile(String name, String corpusName,
			String file) throws Exception {
		DataSource ds = new DataSource(file);
		Instances inst = ds.getDataSet();
		String label = FileUtil.parseLabelFromFileName(inst.relationName());
		Integer run = FileUtil.parseRunFromFileName(inst.relationName());
		Integer fold = FileUtil.parseFoldFromFileName(inst.relationName());
		evaluateAttributes(name, corpusName, inst, label, run, fold);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ytex.weka.WekaAttributeEvaluator#evaluateAttributesFromProps(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void evaluateAttributesFromProps(String name, String corpusName,
			String propFile) throws Exception {
		Properties props = FileUtil.loadProperties(propFile, false);
		if (props != null) {
			sparseDataExporter
					.exportData(props, new WekaAttributeEvaluatorFormatter(
							name, corpusName), null);
		}
	}

	/**
	 * evaluate attributes, store in db
	 * 
	 * @param inst
	 *            instances
	 * @param label
	 *            {@link FeatureEvaluation#getLabel()}
	 * @param run
	 *            {@link ClassifierEvaluation#getRun()} to map to fold
	 * @param fold
	 *            {@link ClassifierEvaluation#getFold()} to map to fold
	 * @throws Exception
	 */
	public void evaluateAttributes(String name, String corpusName,
			Instances inst, String label, Integer run, Integer fold)
			throws Exception {
		AttributeSelection ae = this.getAttributeSelection();
		ae.SelectAttributes(inst);
		double rankedAttributes[][] = ae.rankedAttributes();
		FeatureEvaluation fe = initializeFeatureEvaluation(name, corpusName,
				label, run, fold);
		List<FeatureRank> featureRanks = new ArrayList<FeatureRank>(
				rankedAttributes.length);
		for (int i = 0; i < rankedAttributes.length; i++) {
			int index = (int) rankedAttributes[i][0];
			double eval = rankedAttributes[i][1];
			FeatureRank r = new FeatureRank();
			r.setFeatureName(inst.attribute(index).name());
			r.setRank(i + 1);
			r.setEvaluation(eval);
			featureRanks.add(r);
		}
		fe.setFeatures(featureRanks);
		classifierEvaluationDao.saveFeatureEvaluation(fe);
	}

	public FeatureEvaluation initializeFeatureEvaluation(String name,
			String corpusName, String label, Integer run, Integer fold) {
		FeatureEvaluation fe = new FeatureEvaluation();
		fe.setName(name);
		fe.setEvaluationType(this.getAsEvaluation().getClass().getSimpleName());
		fe.setLabel(label);
		if (run != null && fold != null) {
			CrossValidationFold cvFold = this.classifierEvaluationDao
					.getCrossValidationFold(corpusName, label, run, fold);
			if (cvFold != null)
				fe.setCrossValidationFoldId(cvFold.getCrossValidationFoldId());
			else {
				log.warn("could not obtain cv_fold_id. label=" + label
						+ ", run=" + run + ", fold=" + fold);
			}
		}
		return fe;
	}

	public class WekaAttributeEvaluatorFormatter extends WekaFormatter {
		String name;
		String corpusName;

		public WekaAttributeEvaluatorFormatter(String name, String corpusName) {
			this.name = name;
			this.corpusName = corpusName;
		}

		@Override
		public void exportFold(SparseData sparseData,
				SortedMap<Integer, String> instanceClasses, boolean train,
				String label, Integer run, Integer fold) throws IOException {
			if (train) {
				Instances inst = this.initializeInstances(sparseData,
						instanceClasses, train, label, run, fold);
				try {
					evaluateAttributes(name, corpusName, inst, label, run, fold);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		OptionGroup og = new OptionGroup();
		og.addOption(OptionBuilder
				.withArgName("property file")
				.hasArg()
				.withDescription(
						"use specified property file to load instances for evaluation.  Same format as for SparseDataExporter")
				.create("prop"));
		og.addOption(OptionBuilder
				.withArgName("train_data.arff")
				.hasArg()
				.withDescription(
						"use specified weka arff file to load instances for evaluation.")
				.create("arff"));
		og.setRequired(true);
		options.addOptionGroup(og);
		options.addOption(OptionBuilder.withArgName("name").hasArg()
				.isRequired()
				.withDescription("feature set name (feature_eval.name)")
				.create("name"));
		options.addOption(OptionBuilder
				.withArgName("corpus name")
				.hasArg()
				.withDescription(
						"corpus name (cv_fold.name), used to determine cv_fold_id. If not specified, default to value of name option")
				.create("corpusName"));
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, args);
			String name = line.getOptionValue("name");
			String corpusName = line.getOptionValue("corpusName", name);
			WekaAttributeEvaluator wekaEval = KernelContextHolder
					.getApplicationContext().getBean(
							WekaAttributeEvaluator.class);
			if (line.hasOption("prop")) {
				wekaEval.evaluateAttributesFromProps(name, corpusName,
						line.getOptionValue("prop"));
			} else {
				wekaEval.evaluateAttributesFromFile(name, corpusName,
						line.getOptionValue("arff"));
			}
		} catch (ParseException pe) {
			printHelp(options);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java "
								+ WekaAttributeEvaluatorImpl.class.getName()
								+ " evaluate attributes using a weka AttributeEvaluator",
						options);
	}

}
