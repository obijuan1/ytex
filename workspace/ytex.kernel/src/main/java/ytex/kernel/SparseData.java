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
	 * the instance ids in this sparse data set
	 */
	SortedSet<Long> instanceIds = new TreeSet<Long>();
	/**
	 * instance nominal attribute values
	 */
	Map<Long, SortedMap<String, String>> instanceNominalWords = new HashMap<Long, SortedMap<String, String>>();
	/**
	 * map if instance id to map of attribute name - value pairs
	 */
	Map<Long, SortedMap<String, Double>> instanceNumericWords = new HashMap<Long, SortedMap<String, Double>>();

	/**
	 * nominal attribute names and values
	 */
	SortedMap<String, SortedSet<String>> nominalWordValueMap = new TreeMap<String, SortedSet<String>>();

	/**
	 * numeric attribute labels
	 */
	SortedSet<String> numericWords = new TreeSet<String>();
	
	

	public SortedSet<Long> getInstanceIds() {
		return instanceIds;
	}

	public void setInstanceIds(SortedSet<Long> instanceIds) {
		this.instanceIds = instanceIds;
	}

	public Map<Long, SortedMap<String, String>> getInstanceNominalWords() {
		return instanceNominalWords;
	}

	public Map<Long, SortedMap<String, Double>> getInstanceNumericWords() {
		return instanceNumericWords;
	}

	public SortedMap<String, SortedSet<String>> getNominalWordValueMap() {
		return nominalWordValueMap;
	}

	public SortedSet<String> getNumericWords() {
		return numericWords;
	}

	public void setInstanceNominalWords(
			Map<Long, SortedMap<String, String>> instanceNominalWords) {
		this.instanceNominalWords = instanceNominalWords;
	}

	public void setInstanceNumericWords(
			Map<Long, SortedMap<String, Double>> instanceNumericWords) {
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
