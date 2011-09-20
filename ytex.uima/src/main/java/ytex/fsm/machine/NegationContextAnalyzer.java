package ytex.fsm.machine;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.context.ContextHit;
import edu.mayo.bmi.uima.core.ae.type.NewlineToken;

/**
 * Modified cTAKES NegationContextAnalyzer to filter out newline tokens.
 * @author vijay
 *
 */
public class NegationContextAnalyzer extends
		edu.mayo.bmi.uima.context.negation.NegationContextAnalyzer {
	

	/**
	 * Filter out newline tokens
	 */
	@Override
	public ContextHit analyzeContext(List<? extends Annotation> contextTokens,
			int scopeOrientation) throws AnalysisEngineProcessException {
		List<Annotation> filteredTokens = new ArrayList<Annotation>(contextTokens.size());
		for(Annotation anno : contextTokens) {
			if(!(anno instanceof NewlineToken)) {
				filteredTokens.add(anno);
			}
		}
		return super.analyzeContext(filteredTokens, scopeOrientation);
	}

}
