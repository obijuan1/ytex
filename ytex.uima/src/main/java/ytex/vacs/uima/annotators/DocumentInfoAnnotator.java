package ytex.vacs.uima.annotators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import ytex.vacs.uima.types.DocumentDate;
import ytex.vacs.uima.types.DocumentTitle;
import edu.mayo.bmi.uima.core.ae.type.NewlineToken;
import edu.mayo.bmi.uima.core.ae.type.Segment;

/**
 * annotate the document date and title.
 * Configuration Parameters:
 * <li>regexDocumentTitle list of regular expressions to identify title
 * <li>regexDocumentDate list of regular expressions to identify date
 * <li>lineMax maximum number of lines into the document to look for title and date
 * 
 * Create a DocumentDate annotation for the date.
 * TODO: Get rid of DocumentTitle annotation and just use segment annotation for title
 * @author vijay
 *
 */
public class DocumentInfoAnnotator extends JCasAnnotator_ImplBase {
	private static final Log log = LogFactory
			.getLog(DocumentInfoAnnotator.class);

	int lineMax = 6;
	String regexDocumentTitle[];
	String regexDocumentDate[];
	String[] dateFormats;

	List<Pattern> listDocumentTitlePatterns;
	List<Pattern> listDocumentDatePatterns;
	ThreadLocal<List<DateFormat>> tlDateFormats = new ThreadLocal<List<DateFormat>>() {

		@Override
		protected List<DateFormat> initialValue() {
			List<DateFormat> listDateFormats = new ArrayList<DateFormat>(
					dateFormats.length);
			for (String format : dateFormats) {
				listDateFormats.add(new SimpleDateFormat(format));
			}
			return listDateFormats;
		}

	};

	protected List<Pattern> initPatterns(String regexs[]) {
		List<Pattern> listPatterns = new ArrayList<Pattern>(regexs.length);
		for (String regex : regexs) {
			log.debug("Pattern:" + regex);
			listPatterns.add(Pattern.compile(regex));
		}
		return listPatterns;
	}

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		this.initialize((Integer) aContext.getConfigParameterValue("lineMax"),
				(String[]) aContext
						.getConfigParameterValue("regexDocumentDate"),
				(String[]) aContext
						.getConfigParameterValue("regexDocumentTitle"),
				(String[]) aContext.getConfigParameterValue("dateFormats"));
	}

	/**
	 * this makes unit testing easier
	 * 
	 * @param lineMax
	 * @param regexDocumentDate
	 * @param regexDocumentTitle
	 * @param dateFormats
	 */
	public void initialize(int lineMax, String[] regexDocumentDate,
			String[] regexDocumentTitle, String[] dateFormats) {
		this.lineMax = lineMax;
		this.regexDocumentDate = regexDocumentDate;
		this.regexDocumentTitle = regexDocumentTitle;
		this.dateFormats = dateFormats;
		// initialize threadlocal
		tlDateFormats.get();
		// initialize pattern lists
		this.listDocumentDatePatterns = this.initPatterns(regexDocumentDate);
		this.listDocumentTitlePatterns = this.initPatterns(regexDocumentTitle);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Integer nPosMax = getSearchLimit(aJCas);
		String docText = aJCas.getDocumentText();
		getDocumentTitle(aJCas, nPosMax, docText);
		getDocumentDate(aJCas, nPosMax, docText);
	}

	public DocumentTitle getDocumentTitle(JCas aJCas, Integer nPosMax,
			String docText) {
		for (Pattern p : this.listDocumentTitlePatterns) {
			Matcher matcher = p.matcher(docText);
			if (matcher.find() && matcher.end() < nPosMax
					&& matcher.groupCount() > 0) {
				if (log.isDebugEnabled())
					log.debug("Document Title: " + matcher.group(1));
				if (aJCas != null) {
					// cas can be null for unit testing
					// TODO get rid of DocumentTitle
					DocumentTitle title = new DocumentTitle(aJCas);
					title.setBegin(matcher.start(1));
					title.setEnd(matcher.end(1));
					title.addToIndexes();
					Segment segment = new Segment(aJCas);
					segment.setId("TITLE");
					segment.setBegin(matcher.start(1));
					segment.setEnd(matcher.end(1));
					segment.addToIndexes();
					return title;
				}
			}
		}
		return null;
	}

	public DocumentDate getDocumentDate(JCas aJCas, Integer nPosMax,
			String docText) {
		for (Pattern p : this.listDocumentDatePatterns) {
			Matcher matcher = p.matcher(docText);
			if (matcher.find() && matcher.end() < nPosMax) {
				for (DateFormat df : tlDateFormats.get()) {
					String strDate = matcher.group(1);
					try {
						Date date = df.parse(strDate);
						if (aJCas != null) {
							DocumentDate docDateAnno = new DocumentDate(aJCas);
							docDateAnno.setDate(date.getTime());
							docDateAnno.setBegin(matcher.start(1));
							docDateAnno.setEnd(matcher.end(1));
							docDateAnno.addToIndexes();
							return docDateAnno;
						}
					} catch (ParseException e) {
						log.warn("error parsing date: " + strDate, e);
					}
				}
			}
		}
		return null;
	}

	private int getSearchLimit(JCas aJCas) {
		AnnotationIndex newlineIdx = aJCas
				.getAnnotationIndex(NewlineToken.type);
		FSIterator newlineIter = newlineIdx.iterator();
		NewlineToken newline = null;
		int i = 1;
		while (newlineIter.hasNext() && i < lineMax) {
			newline = (NewlineToken) newlineIter.next();
		}
		if (newline != null)
			return newline.getEnd();
		else
			return 1000; // arbitrary limit
	}

}
