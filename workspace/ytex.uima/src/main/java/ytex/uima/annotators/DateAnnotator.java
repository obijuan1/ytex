package ytex.uima.annotators;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import ytex.uima.types.Date;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;

/**
 * The cTAKES date doesn't actually parse the date. Parse the date with Chronic,
 * store a new annotation with the real date. Takes as initialization parameter
 * a type name; defaults to "edu.mayo.bmi.uima.cdt.ae.type.DateAnnotation"
 * Iterate through all annotations of this type, and use chronic to parse the
 * covered text.
 */
public class DateAnnotator extends JCasAnnotator_ImplBase {
	private static final Log log = LogFactory.getLog(DateAnnotator.class);
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	String dateType;

	private ThreadLocal<SimpleDateFormat> tlDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(DATE_FORMAT);
		}
	};

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		dateType = (String) aContext.getConfigParameterValue("dateType");
		if (dateType == null) {
			dateType = "edu.mayo.bmi.uima.core.type.textsem.DateAnnotation";
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		Type t = jCas.getTypeSystem().getType(dateType);
		if (t != null) {
			AnnotationIndex<Annotation> annoIndex = jCas.getAnnotationIndex();
			FSIterator<Annotation> iter = annoIndex.iterator();
			List<Date> dtList = new ArrayList<Date>();
			while (iter.hasNext()) {
				Annotation anno = iter.next();
				try {
					Span span = Chronic.parse(anno.getCoveredText());
					if (span != null && span.getBeginCalendar() != null) {
						Date date = new Date(jCas);
						date.setBegin(anno.getBegin());
						date.setEnd(anno.getEnd());
						date.setDate(tlDateFormat.get().format(
								span.getBeginCalendar().getTime()));
						dtList.add(date);
					}
				} catch (Exception e) {
					if (log.isDebugEnabled())
						log.debug(
								"chronic failed on: " + anno.getCoveredText(),
								e);
				}
			}
			for(Date date : dtList)
				date.addToIndexes();
		}
	}

}
