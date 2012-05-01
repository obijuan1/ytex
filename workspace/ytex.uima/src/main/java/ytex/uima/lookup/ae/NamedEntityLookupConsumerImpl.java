package ytex.uima.lookup.ae;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import ytex.uima.ApplicationContextHolder;
import ytex.uima.types.OntologyConcept;
import ytex.umls.dao.UMLSDao;
import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.util.TypeSystemConst;
import edu.mayo.bmi.uima.lookup.ae.BaseLookupConsumerImpl;
import edu.mayo.bmi.uima.lookup.ae.LookupConsumer;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TShortSet;
import gnu.trove.set.hash.TShortHashSet;

/**
 * Based on cTAKES NamedEntityLookupConsumerImpl. Modified to filter out
 * duplicate concepts. Modified to use ytex OntologyConcept to support WSD.
 * 
 * Options for RXNORM
 * <ul>
 * <li>[none] - don't check cuis to see if they are from RXNORM
 * <li>ondemand - check in umls.MRCONSO table for each cui to see if it's in
 * RXNORM. will use caching, but this is not efficient.
 * <li>preload - preload all RXNORM CUIs. Checking if a cui is from RXNORM is
 * very fast. recommended for large corpora.
 * 
 * @author vijay
 * 
 */
public class NamedEntityLookupConsumerImpl extends BaseLookupConsumerImpl
		implements LookupConsumer {

	private final String CODE_MF_PRP_KEY = "codeMetaField";

	private final String CODING_SCHEME_PRP_KEY = "codingScheme";
	private final String RXNORM_PRP_KEY = "RXNORM";

	private Properties iv_props;

	private UMLSDao umlsDao = null;
	private TIntSet rxnormSet = null;
	private boolean bOnDemand = false;

	private void initRxnormSet(Properties props) {
		umlsDao = ApplicationContextHolder.getApplicationContext().getBean(
				UMLSDao.class);
		String rxnormOpt = props.getProperty(RXNORM_PRP_KEY);
		if ("preload".equalsIgnoreCase(rxnormOpt)) {
			rxnormSet = umlsDao.getRXNORMCuis();
		} else if ("ondemand".equalsIgnoreCase(rxnormOpt)) {
			bOnDemand = true;
		}
	}

	/**
	 * set the coding scheme to RXNORM for RXNORM cuis
	 * 
	 * @param cui
	 * @return
	 */
	private String getCodingScheme(String cui) {
		if (rxnormSet != null) {
			Matcher m = UMLSDao.cuiPattern.matcher(cui);
			if (m.find()) {
				if (rxnormSet.contains(Integer.parseInt(m.group(1)))) {
					return "RXNORM";
				}
			}
		} else if (bOnDemand && umlsDao.isRXNORMCui(cui)) {
			return "RXNORM";
		}
		return iv_props.getProperty(CODING_SCHEME_PRP_KEY);
	}

	public NamedEntityLookupConsumerImpl(AnnotatorContext aCtx,
			Properties props, int maxListSize) {
		iv_props = props;
		initRxnormSet(props);
	}

	public NamedEntityLookupConsumerImpl(AnnotatorContext aCtx, Properties props) {
		iv_props = props;
		initRxnormSet(props);
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
				oc.setCodingScheme(getCodingScheme(code));
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