package ytex.uima.annotators;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import opennlp.maxent.GISModel;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.tools.sentdetect.DefaultSDContextGenerator;
import opennlp.tools.sentdetect.SentenceDetectorME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.resource.MaxentModelResource;
import edu.mayo.bmi.uima.core.sentence.EndOfSentenceScannerImpl;
import edu.mayo.bmi.uima.core.sentence.type.Sentence;
import edu.mayo.bmi.uima.core.util.ParamUtil;
import gov.va.maveric.uima.section.type.Paragraph;

/**
 * An example annotator that annotates Sentences.
 * @TODO make the regexs for splitting configurable
 */
public class SentenceAnnotator extends JTextAnnotator_ImplBase{
  static abstract class Maker {
    abstract Annotation newAnnotation(JCas jcas, int start, int end);
  }

  JCas jcas;

  String input;

  ParsePosition pp = new ParsePosition(0);
  
	public static final String PARAM_SEGMENTS_TO_SKIP = "SegmentsToSkip";
	
	// LOG4J logger based on class name
	private static final Log logger = LogFactory.getLog(SentenceAnnotator.class);
	
	private final String MAXENT_MODEL_RESRC_KEY = "MaxentModel";
	
	private AnnotatorContext context;
	
	private Set<?> skipSegmentsSet;
	
	private SentenceDetectorME sentenceDetector;
	
	private String newline = "\n";
	
	private int sentenceCount = 0;


  // ****************************************
  // * Static vars holding break iterators
  // ****************************************
  static final BreakIterator sentenceBreak = BreakIterator.getSentenceInstance(Locale.US);

  static final BreakIterator wordBreak = BreakIterator.getWordInstance(Locale.US);

  // *********************************************
  // * function pointers for new instances *
  // *********************************************
  static final Maker sentenceAnnotationMaker = new Maker() {
    Annotation newAnnotation(JCas jcas, int start, int end) {
      return new Sentence(jcas, start, end);
    }
  };
  
  public void initialize(AnnotatorContext aContext) throws AnnotatorConfigurationException,
	AnnotatorInitializationException {

	super.initialize(aContext);
	
	context = aContext;
	try {
		configInit();
	} catch (AnnotatorContextException ace) {
		throw new AnnotatorConfigurationException(ace);
	}
}
  
  /**
   * Reads configuration parameters.
   */
  private void configInit() throws AnnotatorContextException {
	MaxentModelResource mmResrc = (MaxentModelResource) context.getResourceObject(MAXENT_MODEL_RESRC_KEY);
    // <code>SuffixMaxentModelResourceImpl</code> will log the name of the resource at load() time
	// logger.info("Sentence detector resource: " + mmResrc.getModel().toString());

	if (mmResrc == null) {
		// TODO Consider throwing an exception here
		logger.warn("Unable to locate resource with key=" + MAXENT_MODEL_RESRC_KEY + ".");
	} else {
		EndOfSentenceScannerImpl eoss = new EndOfSentenceScannerImpl();
		char [] eosc = eoss.getEndOfSentenceCharacters();
		//SentenceDContextGenerator cg = new SentenceDContextGenerator();
		DefaultSDContextGenerator cg = new DefaultSDContextGenerator(eosc);
		sentenceDetector = new SentenceDetectorME(mmResrc.getModel(), cg, eoss);
	}

	skipSegmentsSet = ParamUtil.getStringParameterValuesSet(PARAM_SEGMENTS_TO_SKIP, context);
}
  
  /**
   * process
   */
//  public void process(JCas aJCas, ResultSpecification resultSpec) throws AnnotatorProcessException {
//    jcas = aJCas;
//    input = jcas.getDocumentText();
//
//    // Create Annotations
//    makeAnnotations(sentenceAnnotationMaker, sentenceBreak);
////    makeAnnotations(tokenAnnotationMaker, wordBreak);
//  }
  
   /**
	* Entry point for processing.
    */
	public void process(JCas jcas, ResultSpecification resultSpec) throws AnnotatorProcessException {
	
	logger.info("Starting processing.");
	
		sentenceCount = 0;
	
		String text = jcas.getDocumentText();
	
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator<?> sectionItr = indexes.getAnnotationIndex(Paragraph.type).iterator();
		while (sectionItr.hasNext()) {
			Paragraph sa = (Paragraph) sectionItr.next();
			String sectionID = sa.getId();
			if (!skipSegmentsSet.contains(sectionID)) {
//					System.out.println("sectionID: " + sectionID);
				sentenceCount = annotateRange(jcas, text, sa, sentenceCount);
//					makeAnnotations(sentenceAnnotationMaker, sentenceBreak);
			}
		}
	}
	
	/**
	 * Detect sentences within a section of the text and add annotations to the CAS.
	 * Uses OpenNLP sentence detector, and then additionally forces sentences to 
	 * end at end-of-line characters (splitting into multiple sentences).
	 * Also trims sentences. And if the sentence detector does happen to 
	 * form a sentence that is just white space, it will be ignored.
	 * @param jcas view of the CAS containing the text to run sentence detector against
	 * @param text the document text
	 * @param section the section this sentence is in
	 * @param sentenceCount the number of sentences added already to the CAS (if
	 * processing one section at a time)
	 * @return count The sum of <code>sentenceCount</code> and the number of Sentence 
	 * annotations added to the CAS for this section
	 * @throws AnnotatorProcessException
	 */
	protected int annotateRange(JCas jcas, String text, Paragraph section, int sentenceCount) 
		throws AnnotatorProcessException {

		
		int b = section.getBegin();
		int e = section.getEnd();


		// Use OpenNLP tools to split text into sentences
		// The sentence detector returns the offsets of the sentence-endings it detects
		// within the string
		int [] sentenceBreaks = sentenceDetector.sentPosDetect(text.substring(b, e));		
		int numSentences = sentenceBreaks.length;
		// There might be text after the last sentence-ending found by detector, so +1
		SentenceSpan[] potentialSentSpans = new SentenceSpan[numSentences+1];
		
		int sentStart = b;
		int sentEnd = b;
		// Start by filling in sentence spans from what OpenNLP tools detected
		// Will trim leading or trailing whitespace when check for end-of-line characters
		for (int i=0; i < numSentences; i++) {
			sentEnd = sentenceBreaks[i]+b;
			String coveredText = text.substring(sentStart, sentEnd);
			potentialSentSpans[i] = new SentenceSpan(sentStart, sentEnd, coveredText);
			sentStart = sentEnd;
		}
		
		// If detector didn't find any sentence-endings, 
		// or there was text after the last sentence-ending found,
		// create a sentence from what's left, as long as it's not all whitespace.
		// Will trim leading or trailing whitespace when check for end-of-line characters
		if (sentEnd<e) {
			String coveredText = text.substring(sentEnd, e);
			if (coveredText.trim()!="") {
				potentialSentSpans[numSentences] = new SentenceSpan(sentEnd, e, coveredText);
				numSentences++;
			}
		}
		
		
		// Copy potentialSentSpans into sentenceSpans,
		// ignoring any that are entirely whitespace,
		// trimming the rest,
		// and splitting any of those that contain an end-of-line character.
		// Then trim any leading or trailing whitespace of ones that were split.
		ArrayList<SentenceSpan> sentenceSpans = new ArrayList<SentenceSpan>(0);
		for (int i=0; i < potentialSentSpans.length; i++) {
			if (potentialSentSpans[i]!=null) {
				sentenceSpans.addAll(potentialSentSpans[i].splitAtLineBreaksAndTrim(newline)); //TODO Determine line break type
			}
		}		
		
		// Add sentence annotations to the CAS
		int previousEnd = -1;
		for (int i = 0; i < sentenceSpans.size(); i++) {
			SentenceSpan span = sentenceSpans.get(i);
			Sentence sa = new Sentence(jcas);
			sa.setBegin(span.getStart());
			sa.setEnd(span.getEnd());
			if (previousEnd<=sa.getBegin()) {
				// System.out.println("Adding Sentence Annotation for " + span.toString());
				sa.setSentenceNumber(sentenceCount);
				sa.addToIndexes();
				sentenceCount++;
				previousEnd = span.getEnd();
			}
			else {
				logger.error("Skipping sentence from " + span.getStart() + " to " + span.getEnd());
				logger.error("Overlap with previous sentence that ended at " + previousEnd);
			}
		}
		return sentenceCount;
	}
  
  /**
	 * Train a new sentence detector from the training data in the first file
	 * and write the model to the second file.<br>
	 * The training data file is expected to have one sentence per line.
	 * 
	 * @param args  training_data_filename name_of_model_to_create iters? cutoff?
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		final Log logger = LogFactory.getLog(SentenceAnnotator.class.getName()+".main()");


		// Handle arguments 
		if (args.length < 2 || args.length > 4) {
			usage(logger);
			System.exit(-1);
		}
		
		File inFile = getReadableFile(args[0]);
		
		File outFile = getFileInExistingDir(args[1]);
		//File outFile = new File(args[1]);

		int iters = 100;
		if (args.length > 2) {
			iters = parseInt(args[2], logger);
		}
		
		int cut = 5;
		if (args.length > 3) {
			cut = parseInt(args[3], logger);
		}
		
		// Now, do the actual training
		EndOfSentenceScannerImpl scanner = new EndOfSentenceScannerImpl();
		int numEosc = scanner.getEndOfSentenceCharacters().length;
		
		logger.info("Training new model from " + inFile.getAbsolutePath());  
		logger.info("Using " + numEosc + " end of sentence characters.");
		GISModel mod = SentenceDetectorME.train(inFile, iters, cut, scanner);			
		SuffixSensitiveGISModelWriter ssgmw = new SuffixSensitiveGISModelWriter(mod, outFile);
		logger.info("Saving the model as: " + outFile.getAbsolutePath());
		ssgmw.persist();	
	}
	
	public static void usage(Log log) {
		log.info("Usage: java " + SentenceAnnotator.class.getName() + 
				" training_data_filename name_of_model_to_create <iters> <cut>");
	}
	
	public static int parseInt(String s, Log log) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			log.error("Unable to parse '" + s + "' as an integer.");
			throw(nfe);
		}
	}
	
	public static File getReadableFile(String fn) throws IOException {
		File f = new File(fn);
		if (!f.canRead()) {
			throw new IOException("Unable to read from file " + f.getAbsolutePath());
		}
		return f;
	}
	
	public static File getFileInExistingDir(String fn) throws IOException {
		File f = new File(fn);
		if (!f.getParentFile().isDirectory()) {
			throw new IOException("Directory not found: " + f.getParentFile().getAbsolutePath());
		}
		return f;
	}

  // *************************************************************
  // * Helper Methods *
  // *************************************************************
  void makeAnnotations(Maker m, BreakIterator b) {
    b.setText(input);
    for (int end = b.next(), start = b.first(); end != BreakIterator.DONE; start = end, end = b
            .next()) {
      // eliminate all-whitespace tokens
      boolean isWhitespace = true;
      for (int i = start; i < end; i++) {
        if (!Character.isWhitespace(input.charAt(i))) {
          isWhitespace = false;
          break;
        }
      }
      if (!isWhitespace) {
        m.newAnnotation(jcas, start, end).addToIndexes();
      }
    }
  }
}
