package ytex.kernel.dao;

import java.util.List;
import java.util.Map;

import ytex.kernel.model.corpus.ConceptLabelStatistic;
import ytex.kernel.model.corpus.CorpusEvaluation;
import ytex.kernel.model.corpus.ConceptInformationContent;
import ytex.kernel.model.corpus.CorpusLabelEvaluation;

public interface CorpusDao {

	public abstract Map<String, Double> getInfoContent(String corpusName,
			String conceptGraphName, String conceptSetName);

	public void addCorpus(CorpusEvaluation eval);

	public void addInfoContent(ConceptInformationContent infoContent);

	public CorpusEvaluation getCorpus(String corpusName,
			String conceptGraphName, String conceptSetName);

	public List<Object[]> getCorpusCuiTuis(String corpusName,
			String conceptGraphName, String conceptSetName);

	void addCorpusLabelEval(CorpusLabelEvaluation eval);

	void addLabelStatistic(ConceptLabelStatistic labelStatistic);

	public abstract CorpusLabelEvaluation getCorpusLabelEvaluation(String corpusName, String conceptGraphName,
			String conceptSetName, String label, Integer foldId);

	public abstract List<ConceptLabelStatistic> getLabelStatistic(String corpusName, String conceptGraphName,
			String conceptSetName, String label, Integer foldId);

}