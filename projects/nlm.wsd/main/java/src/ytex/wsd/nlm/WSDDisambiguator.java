package ytex.wsd.nlm;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import ytex.kernel.ConceptSimilarityService;
import ytex.kernel.ConceptSimilarityService.SimilarityMetricEnum;
import ytex.kernel.KernelContextHolder;
import ytex.kernel.wsd.WordSenseDisambiguator;

public class WSDDisambiguator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimilarityMetricEnum metric = SimilarityMetricEnum.valueOf(args[0]);
		int windowSize = Integer.parseInt(args[1]);
		WSDDisambiguator wsd = new WSDDisambiguator();
		wsd.disambiguate(metric, windowSize);
	}

	public static class Sentence {
		long instanceId = -1;
		int index = -1;
		List<Set<String>> concepts = new ArrayList<Set<String>>();

		public Sentence(long instanceId) {
			super();
			this.instanceId = instanceId;
		}

		public int getIndex() {
			return index;
		}

		public long getInstanceId() {
			return instanceId;
		}

		public void setInstanceId(long instanceId) {
			this.instanceId = instanceId;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public List<Set<String>> getConcepts() {
			return concepts;
		}

		public void setConcepts(List<Set<String>> concepts) {
			this.concepts = concepts;
		}
	}

	public static class Word {
		public long getInstanceId() {
			return instanceId;
		}

		public void setInstanceId(int instanceId) {
			this.instanceId = instanceId;
		}

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}

		public String getCui() {
			return cui;
		}

		public void setCui(String cui) {
			this.cui = cui;
		}

		public int getSpanBegin() {
			return spanBegin;
		}

		public void setSpanBegin(int spanBegin) {
			this.spanBegin = spanBegin;
		}

		public int getSpanEnd() {
			return spanEnd;
		}

		public void setSpanEnd(int spanEnd) {
			this.spanEnd = spanEnd;
		}

		public Word(long instanceId, String word, String cui, int spanBegin,
				int spanEnd) {
			super();
			this.instanceId = instanceId;
			this.word = word;
			this.cui = cui;
			this.spanBegin = spanBegin;
			this.spanEnd = spanEnd;
		}

		long instanceId;
		String word;
		String cui;
		int spanBegin;
		int spanEnd;
	}

	Map<String, Set<String>> wordCuis;
	Map<Long, Word> words;
	JdbcTemplate jdbcTemplate;
	SortedMap<Long, Sentence> sentences;
	WordSenseDisambiguator wordSenseDisambiguator;

	public WSDDisambiguator() {
		DataSource ds = (DataSource)KernelContextHolder.getApplicationContext().getBean("dataSource");
		this.jdbcTemplate = new JdbcTemplate(ds);
		this.wordSenseDisambiguator = KernelContextHolder.getApplicationContext().getBean(WordSenseDisambiguator.class);
	}
	
	public Map<String, Set<String>> loadWordCuis() {
		wordCuis = new HashMap<String, Set<String>>();
		jdbcTemplate.query("select word, cui from nlm_wsd_cui order by word",
				new RowCallbackHandler() {
					String wordCurrent = null;
					Set<String> cuisCurrent = null;

					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String wordNew = rs.getString(1);
						String cui = rs.getString(2);
						if (wordCurrent == null || !wordCurrent.equals(wordNew)) {
							cuisCurrent = new HashSet<String>();
							wordCurrent = wordNew;
							wordCuis.put(wordCurrent, cuisCurrent);
						}
						cuisCurrent.add(cui);
					}
				});
		return wordCuis;
	}

	public Map<Long, Word> loadWords() {
		words = new HashMap<Long, Word>();
		jdbcTemplate
				.query("select w.instance_id, w.word, coalesce(c.cui, w.concept_code) cui, w.sent_ambiguity_start spanBegin, w.sent_ambiguity_end+1 spanEnd from nlm_wsd w left join nlm_wsd_cui c on w.word = c.word",
						new RowCallbackHandler() {
							@Override
							public void processRow(ResultSet rs)
									throws SQLException {
								Word w = new Word(rs.getLong(1), rs
										.getString(2), rs.getString(3), rs
										.getInt(4), rs.getInt(5));
								words.put(w.getInstanceId(), w);
							}
						});
		return words;
	}

	public Map<Long, Sentence> loadSentences() {
		sentences = new TreeMap<Long, Sentence>();
		jdbcTemplate
				.query("select d.uid instance_id, b.span_begin, b.span_end, c.code from document d inner join anno_base b on d.document_id = b.document_id inner join anno_ontology_concept c on c.anno_base_id = b.anno_base_id where d.analysis_batch = 'wsd-sentence' order by d.uid, span_begin, span_end",
						new RowCallbackHandler() {
							int currentSpanBegin = -1;
							int currentSpanEnd = -1;
							Sentence currentSentence = null;
							Set<String> currentConcepts = null;
							int currentConceptIndex = -1;

							@Override
							public void processRow(ResultSet rs)
									throws SQLException {
								long instanceId = rs.getLong(1);
								int spanBegin = rs.getInt(2);
								int spanEnd = rs.getInt(3);
								String cui = rs.getString(4);
								if (currentSentence == null
										|| currentSentence.getInstanceId() != instanceId) {
									// new word
									reset(instanceId);
								}
								if (currentConcepts == null
										|| currentSpanBegin != spanBegin
										|| currentSpanEnd != spanEnd) {
									// new concept
									resetCurrentConcepts(spanBegin, spanEnd);
								}
								// don't touch the concept that's supposed to
								// be disambiguated
								if (currentConceptIndex != currentSentence
										.getIndex()) {
									currentConcepts.add(cui);
								}
							}

							private void resetCurrentConcepts(int spanBegin,
									int spanEnd) {
								// increment index
								currentConceptIndex++;
								// allocate new set for concepts
								currentConcepts = new HashSet<String>();
								// add the set to the sentence
								currentSentence.getConcepts().add(
										currentConcepts);
								// see if this concept is the target for
								// disambiguation
								Word w = words.get(currentSentence
										.getInstanceId());
								if (w.getSpanBegin() == spanBegin
										&& w.spanEnd == spanEnd) {
									// bingo
									currentSentence
											.setIndex(currentConceptIndex);
									currentConcepts.addAll(wordCuis.get(w
											.getWord()));
								}
							}

							private void reset(long instanceId) {
								currentSpanBegin = -1;
								currentSpanEnd = -1;
								currentSentence = new Sentence(instanceId);
								currentConcepts = null;
								currentConceptIndex = -1;
								sentences.put(instanceId, currentSentence);
							}
						});
		return sentences;
	}
	
	public void disambiguate(ConceptSimilarityService.SimilarityMetricEnum metric, int windowSize) {
		this.loadWordCuis();
		this.loadWords();
		this.loadSentences();
		PrintStream ps = System.out;
		for(Map.Entry<Long, Sentence> sentEntry : sentences.entrySet()) {
			long instanceId = sentEntry.getKey();
			Sentence s = sentEntry.getValue();
			Word w = words.get(instanceId);
			ps.print(instanceId);
			ps.print("\t");
			ps.print(w.getWord());
			ps.print("\t");
			ps.print(w.getCui());
			ps.print("\t");
			Map<String, Double> scoreMap = new HashMap<String,Double>();
			String cui = this.wordSenseDisambiguator.disambiguate(s.getConcepts(), s.getIndex(), null, windowSize, metric, scoreMap);
			ps.print(cui);
			ps.print("\t");
			ps.println(scoreMap);
		}
	}
}
