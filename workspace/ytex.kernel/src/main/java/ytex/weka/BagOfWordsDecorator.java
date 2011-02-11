package ytex.weka;

import java.util.Map;
import java.util.Set;

/**
 * Classes that delegate to the BagOfWordsExporter can pass this decorator in to
 * add additional attributes
 * 
 * @author vijay
 * 
 */
public interface BagOfWordsDecorator {
	public void decorateNumericInstanceWords(
			Map<Integer, Map<String, Double>> instanceNumericWords,
			Set<String> numericWords);

	public void decorateNominalInstanceWords(
			Map<Integer, Map<String, String>> instanceNominalWords,
			Map<String, Set<String>> nominalWordValueMap);
}
