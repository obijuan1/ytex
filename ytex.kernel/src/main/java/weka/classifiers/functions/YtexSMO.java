package weka.classifiers.functions;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import weka.classifiers.functions.supportVector.SMOset;
import weka.core.AdditionalMeasureProducer;

/**
 * Extend SMO to return number of support vectors. Useful for model selection.
 * 
 * @author vijay
 * 
 */
public class YtexSMO extends SMO implements AdditionalMeasureProducer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Field fieldSV = null;

	/**
	 * hack so we can get at the protected supportVectors
	 */
	static {
	}

	public YtexSMO() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration enumerateMeasures() {
		Vector newVector = new Vector(1);
		newVector.addElement("measureNumSupportVectors");
		return newVector.elements();
	}

	@Override
	public double getMeasure(String measureName) {
		if ("measureNumSupportVectors".equals(measureName))
			return getNumSupportVectors();
		else {
			throw new IllegalArgumentException(measureName
					+ " not supported (YtexSMO)");
		}
	}

	private double getNumSupportVectors() {
		Set<Integer> svIndices = new HashSet<Integer>();
		for (BinarySMO[] bsmoArray : m_classifiers) {
			for (BinarySMO bsmo : bsmoArray) {
				try {
					if (bsmo != null) {
						SMOset supportVectors = getSMOSet(bsmo);
						for (int i = supportVectors.getNext(-1); i != -1; i = supportVectors
								.getNext(i)) {
							svIndices.add(i);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return svIndices.size();
	}

	private SMOset getSMOSet(BinarySMO bsmo) {
		String fieldName = "m_supportVectors";
		Field fields[] = bsmo.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			if (fieldName.equals(fields[i].getName())) {
				fields[i].setAccessible(true);
				try {
					return (SMOset) fields[i].get(bsmo);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Returns a string describing classifier
	 * 
	 * @return a description suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "Extend SMO to return number of support vectors.  Useful for model selection.\n"
				+ super.globalInfo();
	}
}
