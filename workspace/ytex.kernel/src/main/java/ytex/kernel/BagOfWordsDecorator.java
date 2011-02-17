package ytex.kernel;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Classes that delegate to the BagOfWordsExporter can pass this decorator in to
 * add additional attributes
 * 
 * @author vijay
 * 
 */
public interface BagOfWordsDecorator {
	public void decorateNumericInstanceWords(
			Map<Integer, SortedMap<String, Double>> instanceNumericWords,
			SortedSet<String> numericWords);

	public void decorateNominalInstanceWords(
			Map<Integer, SortedMap<String, String>> instanceNominalWords,
			Map<String, SortedSet<String>> nominalWordValueMap);
}
