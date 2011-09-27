package ytex.kernel;

import java.io.IOException;

public interface BagOfWordsExporter {

	/**
	 * 
	 * @param propertyFile
	 *            .xml/.properties file with following properties:
	 *            <ul>
	 *            <li>
	 *            arffRelation (see exportBagOfWords)
	 *            <li>instanceClassQuery (see exportBagOfWords)
	 *            <li>
	 *            numericWordQuery (see exportBagOfWords)
	 *            <li>nominalWordQuery (see exportBagOfWords)
	 *            <li>arffFile file name to write arff file to
	 *            </ul>
	 * @throws IOException
	 */
	public abstract void exportBagOfWords(String propertyFile)
			throws IOException;

}