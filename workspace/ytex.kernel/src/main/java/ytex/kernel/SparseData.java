package ytex.kernel;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure populated by SparseDataExporter that has all the instance
 * attributes needed for exporting to various formats.
 * 
 * @author vijay
 * 
 */
public class SparseData {
	/**
	 * instance nominal attribute values
	 */
	Map<Integer, SortedMap<String, String>> instanceNominalWords = new HashMap<Integer, SortedMap<String, String>>();
	/**
	 * map if instance id to map of attribute name - value pairs
	 */
	Map<Integer, SortedMap<String, Double>> instanceNumericWords = new HashMap<Integer, SortedMap<String, Double>>();


	/**
	 * nominal attribute names and values
	 */
	SortedMap<String, SortedSet<String>> nominalWordValueMap = new TreeMap<String, SortedSet<String>>();

	/**
	 * numeric attribute labels
	 */
	SortedSet<String> numericWords = new TreeSet<String>();



	public Map<Integer, SortedMap<String, String>> getInstanceNominalWords() {
		return instanceNominalWords;
	}
	public Map<Integer, SortedMap<String, Double>> getInstanceNumericWords() {
		return instanceNumericWords;
	}
	public SortedMap<String, SortedSet<String>> getNominalWordValueMap() {
		return nominalWordValueMap;
	}
	public SortedSet<String> getNumericWords() {
		return numericWords;
	}

	public void setInstanceNominalWords(
			Map<Integer, SortedMap<String, String>> instanceNominalWords) {
		this.instanceNominalWords = instanceNominalWords;
	}

	public void setInstanceNumericWords(
			Map<Integer, SortedMap<String, Double>> instanceNumericWords) {
		this.instanceNumericWords = instanceNumericWords;
	}

	public void setNominalWordValueMap(
			SortedMap<String, SortedSet<String>> nominalWordValueMap) {
		this.nominalWordValueMap = nominalWordValueMap;
	}

	public void setNumericWords(SortedSet<String> numericWords) {
		this.numericWords = numericWords;
	}

}
