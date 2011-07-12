package ytex.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapped to document table.
 * Contains document text (from JCas.getDocumentText()).
 * Contains gzipped xmi CAS.
 * @author vijay
 *
 */
public class Document implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the document id
	 */
	Integer documentID;

	/**
	 * document plain text
	 */
	String docText;

	/**
	 * the document CAS - serialised in XMI format and compressed.
	 */
	byte[] cas;

	List<DocumentAnnotation> documentAnnotations = new ArrayList<DocumentAnnotation>();

	List<DocumentClass> documentClasses = new ArrayList<DocumentClass>();

	String analysisBatch;

	public Document() {
		super();
	}

	public byte[] getCas() {
		return cas;
	}

	public void setCas(byte[] cas) {
		this.cas = cas;
	}

	public Integer getDocumentID() {
		return documentID;
	}

	public void setDocumentID(Integer documentID) {
		this.documentID = documentID;
	}

	public List<DocumentAnnotation> getDocumentAnnotations() {
		return documentAnnotations;
	}

	public void setDocumentAnnotations(
			List<DocumentAnnotation> documentAnnotations) {
		this.documentAnnotations = documentAnnotations;
	}

	public String getAnalysisBatch() {
		return analysisBatch;
	}

	public void setAnalysisBatch(String analysisBatch) {
		this.analysisBatch = analysisBatch;
	}

	public String getDocText() {
		return docText;
	}

	public void setDocText(String docText) {
		this.docText = docText;
	}

	public List<DocumentClass> getDocumentClasses() {
		return documentClasses;
	}

	public void setDocumentClasses(List<DocumentClass> documentClasses) {
		this.documentClasses = documentClasses;
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName() + " [documentID="
				+ documentID + ", documentAnnotations=" + documentAnnotations
				+ "]";
	}
}
