package ytex.uima.annotators;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.FileReader;

import ytex.vacs.uima.annotators.DocumentInfoAnnotator;

import junit.framework.TestCase;

public class DocumentInfoAnnotatorTest extends TestCase {
	DocumentInfoAnnotator docInfoAnnotator = new DocumentInfoAnnotator();
	String docText = null;

	// Procedure: ABDOMEN, LIVER (US)
	// Exam Date: Dec 25, 2001@08:54
	// MMM dd, YYYY@HH:mm
	// \\w\\w\\w\\s\\d\\d,\\s\\d\\d\\d\\d@\\d\\d:\\d\\d

	protected void setUp() throws Exception {
		super.setUp();
		docInfoAnnotator.initialize(7, new String[] {"Date:\\s(\\w\\w\\w\\s\\d\\d,\\s\\d\\d\\d\\d@\\d\\d:\\d\\d)"}/* regexDocumentDate */
		, new String[] {"Procedure:\\s*(\\w[^\\n\\r]+)"}/* regexDocumentTitle */
		, new String[] {"MMM dd, yyyy@HH:mm"} /* dateFormats */);
		CharArrayWriter writer = new CharArrayWriter();
		BufferedReader reader = new BufferedReader(new FileReader("../data/testnotes/export/test2.txt"));
		int c = 0;
		while((c = reader.read()) != -1) {
			writer.write(c);
		}
		reader.close();
		writer.close();
		docText = new String(writer.toCharArray());
	}

	public void testGetDocumentTitle() {
		docInfoAnnotator.getDocumentTitle(null, 1000, docText);
	}

	public void testGetDocumentDate() {
		docInfoAnnotator.getDocumentDate(null, 1000, docText);
	}

}
