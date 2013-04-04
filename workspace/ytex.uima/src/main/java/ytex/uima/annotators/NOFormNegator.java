package ytex.uima.annotators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * Create IdentifiedAnnotation annotations. Use regex to identify the Named Entities.
 * Read the named entity regex - concept id map from the db.
 * 
 * @author vijay
 * 
 */
public class NOFormNegator extends JCasAnnotator_ImplBase {
	private static final Log log = LogFactory.getLog(NOFormNegator.class);

	private static final Pattern noPattern = Pattern
			.compile("(?im)\\A\\s*:\\s*no\\b|\\A\\.{0,3}\\s*:\\s*\\[\\s*x\\s*\\]\\s*no\\b|\\A\\s*\\?\\s*yes\\s*\\[\\s*\\]\\s*no\\s*\\[\\s*x\\s*\\]|\\A\\.{0,3}\\s*:\\s*\\[\\s*]\\s*yes\\s*\\[\\s*x{0,1}\\s*\\]\\s*no\\b|\\A\\.{0,3}\\s*:\\s*yes\\s*\\[\\s*\\]\\s*no\\s*\\[\\s*x{0,1}\\s*\\]|\\s*:\\A\\s*no\\s*\\r{0,1}\\n");
	/**
	 * []INF []REF []DEL-
	 */
	private static final Pattern noPrefixPattern = Pattern
			.compile("(?im)(\\w*)\\s*[\\[|\\(]\\s*[on]{0,2}\\s*[\\]|\\)]\\s*\\z|\\[\\s*\\]INF\\s*\\[\\s*\\]REF\\s*\\[\\s*\\]DEL\\s*-\\s*\\z");
	// private static final Pattern yesPattern = Pattern
	// .compile("(?im)\\A\\s*:\\s*yes\\b|\\A\\.{0,3}\\s*:\\s*\\[\\s*x\\s*\\]\\s*yes\\s*\\[\\s*]\\s*no\\b|\\A\\.{0,3}\\s*:\\s*yes\\s*\\[\\s*x{0,1}\\s*\\]\\s*no\\s*\\[\\s*\\]|\\s*:\\s*yes\\s*\\r{0,1}\\n");
	private static final Pattern yesPattern = Pattern
			.compile("(?im)\\A\\s*:\\s*yes\\b|\\A\\.{0,3}\\s*:\\s*\\[\\s*x\\s*\\]\\s*yes\\s*\\[\\s*]\\s*no\\b|\\A\\.{0,3}\\s*:\\s*yes\\s*\\[\\s*x{0,1}\\s*\\]\\s*no\\s*\\[\\s*\\]");
	private static final Pattern yesPrefixPattern = Pattern
			.compile("(?im)(\\w*)\\s*[\\[|\\(]\\s*[yesx]{1,3}\\s*[\\]|\\)]\\s*\\z|\\bproblem[\\s\\w]*:\\s*\\z|\\baxis[ \\w]*:[ \\w,]*\\z");
	private static final Pattern maybePattern = null;
	/**
	 * [x]INF []REF []DEL-
	 * [x]INF [x]REF []DEL- 
	 * [x ]INF [x]REF [x]DEL- 
	 */
	private static final Pattern maybePrefixPattern = Pattern
			.compile("(?im)\\[\\s*x\\s*\\]INF\\s*\\[\\s*x{0,1}\\s*\\]REF\\s*\\[\\s*x{0,1}\\s*\\]DEL\\s*-\\s*\\z");

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String docText = aJCas.getDocumentText();
		FSIterator<Annotation> neIterator = aJCas.getAnnotationIndex(
				IdentifiedAnnotation.type).iterator();
		// iterate through named entities
		while (neIterator.hasNext()) {
			boolean matched = false;
			IdentifiedAnnotation ne = (IdentifiedAnnotation)neIterator.next();
			// x, no, and yes get matched as entities
			if (ne.getCoveredText().equalsIgnoreCase("x")
					|| ne.getCoveredText().equalsIgnoreCase("no")
					|| ne.getCoveredText().equalsIgnoreCase("yes"))
				continue;
			// checked for a negated form after this named entity
			matched = checkFormSuffix(docText, noPattern, -1, 100, ne);
			if (!matched) {
				// checked for a negated form preceding this named entity
				matched = checkFormPrefix(docText, noPrefixPattern, -1, 100, ne);
			}
			if (!matched) {
				// checked for a affirmed form after this named entity
				matched = checkFormSuffix(docText, yesPattern, 0, 100, ne);
			}
			if (!matched) {
				// checked for a affirmed form preceding this named entity
				matched = checkFormPrefix(docText, yesPrefixPattern, 0, 100, ne);
			}
			if (!matched) {
				// checked for a affirmed form preceding this named entity
				matched = checkFormPrefix(docText, maybePrefixPattern, 0, 50, ne);
			}
		}
	}

	private boolean checkFormPrefix(String docText, Pattern prefixPattern,
			int polarity, int confidence, IdentifiedAnnotation ne) {
		// check for a negated form preceding the named entity
		boolean matched = false;
		if (ne.getBegin() > 0) {
			Matcher matcher = prefixPattern.matcher(docText.substring(0, ne
					.getBegin()));
			if (matcher.find()) {
				String previousWord = matcher.groupCount() > 0 ? matcher.group(1) : null;
				if (!(previousWord != null && previousWord.length() > 0 && (previousWord
						.equalsIgnoreCase("yes") || previousWord
						.equalsIgnoreCase("no")))) {
					ne.setPolarity(polarity);
					ne.setConfidence(confidence);
					matched = true;
				}
			}
		}
		return matched;
	}

	private boolean checkFormSuffix(String docText, Pattern pattern,
			int polarity, int confidence, IdentifiedAnnotation ne) {
		boolean matched = false;
		if (ne.getEnd() < docText.length()) {
			// if the string following the named entity matches the regex,
			// negate the named entity
			Matcher matcher = pattern.matcher(docText.substring(ne.getEnd()));
			if (matcher.find()) {
				ne.setPolarity(polarity);
				ne.setConfidence(confidence);
				matched = true;
			}
		}
		return matched;
	}
}
