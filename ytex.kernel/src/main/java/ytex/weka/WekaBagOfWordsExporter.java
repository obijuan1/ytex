package ytex.weka;

import java.io.BufferedWriter;
import java.io.IOException;

import ytex.kernel.BagOfWordsDecorator;
import ytex.kernel.BagOfWordsExporter;

/**
 * @author vhacongarlav
 * 
 */
public interface WekaBagOfWordsExporter extends BagOfWordsExporter {

	/**
	 * @param arffRelation
	 *            relation of arff file to generate
	 * @param instanceClassQuery
	 *            query with result columns: column 1 - integer instance id,
	 *            column 2 - string class label
	 * @param numericWordQuery
	 *            query with result colums: column 1 - integer instance id,
	 *            column 2 - word, column 3 - numeric word value
	 * @param nominalWordQuery
	 *            query with result colums: column 1 - integer instance id,
	 *            column 2 - word, column 3 - string word value
	 * @param writer
	 *            where arff file will be written
	 * @throws IOException
	 */
	public abstract void exportBagOfWords(String arffRelation,
			String instanceClassQuery, String numericWordQuery,
			String nominalWordQuery, BufferedWriter writer) throws IOException;

	public abstract void exportBagOfWords(String propertyFile,
			BagOfWordsDecorator bDecorator) throws IOException;

}