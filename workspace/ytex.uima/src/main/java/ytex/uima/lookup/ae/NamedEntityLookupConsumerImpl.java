package ytex.uima.lookup.ae;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import ytex.uima.types.OntologyConcept;
import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.util.TypeSystemConst;
import edu.mayo.bmi.uima.lookup.ae.BaseLookupConsumerImpl;
import edu.mayo.bmi.uima.lookup.ae.LookupConsumer;

/**
 * Based on cTAKES NamedEntityLookupConsumerImpl. Modified to filter out
 * duplicate concepts. Modified to use ytex OntologyConcept to support WSD.
 * 
 * @author vijay
 * 
 */
public class NamedEntityLookupConsumerImpl extends BaseLookupConsumerImpl
		implements LookupConsumer {

	private final String CODE_MF_PRP_KEY = "codeMetaField";

	private final String CODING_SCHEME_PRP_KEY = "codingScheme";

	private Properties iv_props;

	private static int iv_maxSize;

	public NamedEntityLookupConsumerImpl(AnnotatorContext aCtx,
			Properties props, int maxListSize) {
		// TODO property validation could be done here
		iv_props = props;
		iv_maxSize = maxListSize;
	}

	public NamedEntityLookupConsumerImpl(AnnotatorContext aCtx, Properties props) {
		// TODO property validation could be done here
		iv_props = props;
	}

	public void consumeHits(JCas jcas, Iterator lhItr)
			throws AnnotatorProcessException {
		Iterator hitsByOffsetItr = organizeByOffset(lhItr);
		while (hitsByOffsetItr.hasNext()) {
			Collection hitsAtOffsetCol = (Collection) hitsByOffsetItr.next();

			// iterate over the LookupHit objects and create
			// a corresponding JCas OntologyConcept object that will
			// be placed in a FSArray
			Iterator lhAtOffsetItr = hitsAtOffsetCol.iterator();
			int neBegin = -1;
			int neEnd = -1;
			Set<String> concepts = new HashSet<String>();
			while (lhAtOffsetItr.hasNext()) {
				LookupHit lh = (LookupHit) lhAtOffsetItr.next();
				neBegin = lh.getStartOffset();
				neEnd = lh.getEndOffset();
				MetaDataHit mdh = lh.getDictMetaDataHit();
				String code = mdh.getMetaFieldValue(iv_props
						.getProperty(CODE_MF_PRP_KEY));
				concepts.add(code);
			}
			FSArray ocArr = new FSArray(jcas, concepts.size());
			int ocArrIdx = 0;
			for (String code : concepts) {
				OntologyConcept oc = new OntologyConcept(jcas);
				// set the cui field if this is in fact a cui
				oc.setCode(code);
				oc.setCodingScheme(iv_props.getProperty(CODING_SCHEME_PRP_KEY));
				ocArr.set(ocArrIdx, oc);
				ocArrIdx++;
			}

			NamedEntity neAnnot = new NamedEntity(jcas);
			neAnnot.setBegin(neBegin);
			neAnnot.setEnd(neEnd);
			neAnnot.setDiscoveryTechnique(TypeSystemConst.NE_DISCOVERY_TECH_DICT_LOOKUP);
			neAnnot.setOntologyConceptArr(ocArr);
			neAnnot.addToIndexes();
		}
	}
}