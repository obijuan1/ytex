package ytex.uima.annotators;

import java.util.HashMap;
import java.util.Map;
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
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import ytex.dao.NamedEntityRegexDao;
import ytex.model.NamedEntityRegex;
import ytex.uima.ApplicationContextHolder;
import edu.mayo.bmi.uima.core.ae.type.NamedEntity;
import edu.mayo.bmi.uima.core.ae.type.OntologyConcept;
import edu.mayo.bmi.uima.core.ae.type.Segment;

/**
 * Create NamedEntity annotations.
 * Use regex to identify the Named Entities.
 * Read the named entity regex - concept id map from the db.
 * @author vijay
 *
 */
public class NamedEntityRegexAnnotator extends JCasAnnotator_ImplBase {
	private static final Log log = LogFactory
			.getLog(NamedEntityRegexAnnotator.class);

	private NamedEntityRegexDao neRegexDao;
	private Map<NamedEntityRegex, Pattern> regexMap;

//	private Integer getTypeIdForClassName(String strClassName) {
//		try {
//			Class<?> clazz = Class.forName(strClassName);
//			Field field = clazz.getDeclaredField("typeIndexID");
//			return field.getInt(clazz);
//		} catch (Exception e) {
//			log.error("config error, could not get type id for class: "
//					+ strClassName, e);
//			return null;
//		}
//	}

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		neRegexDao = (NamedEntityRegexDao) ApplicationContextHolder
				.getApplicationContext().getBean("namedEntityRegexDao");
		regexMap = new HashMap<NamedEntityRegex, Pattern>();
		for (NamedEntityRegex regex : neRegexDao.getNamedEntityRegexs()) {
			if (log.isDebugEnabled())
				log.debug(regex);
			Pattern pat = Pattern.compile(regex.getRegex());
			regexMap.put(regex, pat);
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		for (Map.Entry<NamedEntityRegex, Pattern> entry : regexMap.entrySet()) {
			if (entry.getKey().getContext() != null) {
				// if a context is specified, look only in instances of the
				// given context
//				Integer nTypeId = getTypeIdForClassName(entry.getKey()
//						.getContext());
//				if (nTypeId != null) {
//					AnnotationIndex idx = aJCas.getAnnotationIndex(nTypeId);
//					FSIterator iter = idx.iterator();
//					while (iter.hasNext()) {
//						Annotation anno = (Annotation) iter.next();
//						processRegex(aJCas, anno, entry.getKey(), entry
//								.getValue());
//					}
//				}
				AnnotationIndex idx = aJCas.getAnnotationIndex(Segment.typeIndexID);
				FSIterator iter = idx.iterator();
				while(iter.hasNext()) {
					Segment segment = (Segment)iter.next();
					if(entry.getKey().getContext().equals(segment.getId())) {
						processRegex(aJCas, segment, entry.getKey(), entry.getValue());
					}
				}
			} else {
				// no context specified - search entire document
				processRegex(aJCas, null, entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Search the document / annotation span for with the supplied pattern. If
	 * we get a hit, create a named entity annotation.
	 * 
	 * @param aJCas
	 * @param anno
	 * @param neRegex
	 * @param pattern
	 */
	private void processRegex(JCas aJCas, Annotation anno,
			NamedEntityRegex neRegex, Pattern pattern) {
		String docText = aJCas.getDocumentText();
		String annoText = anno != null ? docText.substring(anno.getBegin(),
				anno.getEnd()) : docText;
		int nOffset = anno != null ? anno.getBegin() : 0;
		Matcher matcher = pattern.matcher(annoText);
		while (matcher.find()) {
			NamedEntity ne = new NamedEntity(aJCas);
			ne.setBegin(nOffset + matcher.start());
			ne.setEnd(nOffset + matcher.end());
			FSArray ocArr = new FSArray(aJCas, 1);
			OntologyConcept oc = new OntologyConcept(aJCas);
			oc.setCode(neRegex.getCode());
			oc.setCodingScheme(neRegex.getCodingScheme());
			oc.setOid(neRegex.getOid());
			ocArr.set(0, oc);
			ne.setOntologyConceptArr(ocArr);
			ne.addToIndexes();
		}
	}
}
