package ytex.uima.lookup.ae;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.jcas.tcas.Annotation;

import ytex.tools.SetupAuiFirstWord;
import edu.mayo.bmi.lookup.vo.LookupAnnotation;
import edu.mayo.bmi.lookup.vo.LookupToken;
import edu.mayo.bmi.uima.core.type.syntax.WordToken;

/**
 * allow dictionary lookup with stemmed words
 * 
 * @author vijay
 * 
 */
public class StemmedLookupAnnotationToJCasAdapter implements LookupAnnotation,
		LookupToken {
	private Map<String, String> iv_attrMap = new HashMap<String, String>();

	private Annotation iv_jcasAnnotObj;

	public StemmedLookupAnnotationToJCasAdapter(Annotation jcasAnnotObj) {
		iv_jcasAnnotObj = jcasAnnotObj;
	}

	public void addStringAttribute(String attrKey, String attrVal) {
		iv_attrMap.put(attrKey, attrVal);
	}

	public int getEndOffset() {
		return iv_jcasAnnotObj.getEnd();
	}

	public int getLength() {
		return getStartOffset() - getEndOffset();
	}

	public int getStartOffset() {
		return iv_jcasAnnotObj.getBegin();
	}

	public String getStringAttribute(String attrKey) {
		return (String) iv_attrMap.get(attrKey);
	}

	/**
	 * if this is a word, return the stemmed word, if available - i.e. canonicalForm not null and not empty.
	 * else return the covered text.
	 * @see SetupAuiFirstWord
	 */
	public String getText() {
		if (iv_jcasAnnotObj instanceof WordToken) {
			WordToken wt = (WordToken) iv_jcasAnnotObj;
			if (wt.getCanonicalForm() != null && wt.getCanonicalForm().length() > 0)
				return wt.getCanonicalForm();
		}
		return iv_jcasAnnotObj.getCoveredText();
	}

}