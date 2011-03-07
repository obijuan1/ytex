package ytex.kernel.dao;

import java.util.List;

import org.hibernate.Query;

import ytex.kernel.model.Corpus;
import ytex.kernel.model.CorpusTerm;
import ytex.kernel.model.InfoContent;

public interface CorpusDao {

	// public abstract Corpus updateCorpusTermFrequency(String corpusName,
	// Set<String> analysisBatches);

	public abstract List<CorpusTerm> getTerms(String corpusName);

	public void addInfoContent(InfoContent infoContent);

	public Corpus getCorpus(String corpusName);

	public List<InfoContent> getInfoContent(List<String> corpusNames);

	public List<Object[]> getCorpusCuiTuis(String corpusName);

}