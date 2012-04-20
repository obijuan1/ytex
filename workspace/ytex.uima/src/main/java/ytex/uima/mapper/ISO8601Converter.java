package ytex.uima.mapper;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

import ytex.uima.annotators.DateAnnotator;

/**
 * convert ISO8601 formatted date to Date/Timestamp object
 * @author vijay
 *
 */
public class ISO8601Converter implements Converter {
	private ThreadLocal<SimpleDateFormat> tlDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(DateAnnotator.DATE_FORMAT);
		}
	};

	@Override
	public Object convert(Class targetClass, Object input)
			throws ConversionException {
		if (!(input instanceof String)) {
			throw new ConversionException("input not a string: "
					+ input.getClass());
		}
		Date dt;
		try {
			dt = tlDateFormat.get().parse((String) input);
			if (targetClass.equals(Date.class))
				return dt;
			else if (targetClass.equals(Timestamp.class))
				return new Timestamp(dt.getTime());
			else
				throw new ConversionException("bad target type: "
						+ targetClass.getName());
		} catch (ParseException e) {
			throw new ConversionException(e);
		}
	}

}
