package ytex.uima.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.jcas.tcas.Annotation;

import ytex.model.Document;
import ytex.uima.types.Date;
import ytex.model.DateAnnotation;

/**
 * convert the string date to a real date.
 * @author vhacongarlav
 *
 */
public class DateMapper extends
		AbstractDocumentAnnotationMapper<DateAnnotation, Date> {
	Log log = LogFactory.getLog(DateMapper.class);
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	private ThreadLocal<SimpleDateFormat> tlDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		}
	};

	DateMapper() {
		super(DateAnnotation.class, Date.class);
	}

	@Override
	public void mapAnnotationProperties(DateAnnotation anno,
			Annotation uimaAnno, Document doc) {
		//super.mapAnnotationProperties(anno, uimaAnno, doc);
		anno.setBegin(uimaAnno.getBegin());
		anno.setEnd(uimaAnno.getEnd());
		Date date = (Date) uimaAnno;
		if (date.getDate() != null && date.getDate().length() > 0) {
			try {
				anno.setDate(tlDateFormat.get().parse(date.getDate()));
			} catch (ParseException e) {
				log.error("error parsing date: " + date.getDate(), e);
			}
		}
	}

}
