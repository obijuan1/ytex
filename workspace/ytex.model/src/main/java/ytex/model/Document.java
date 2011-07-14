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

	String analysisBatch;

	/**
	 * the document CAS - serialised in XMI format and compressed.
	 */
	byte[] cas;

	/**
	 * document plain text
	 */
	String docText;

	List<DocumentAnnotation> documentAnnotations = new ArrayList<DocumentAnnotation>();

	List<DocumentClass> documentClasses = new ArrayList<DocumentClass>();

	/**
	 * the document id
	 */
	Integer documentID;
	
	/**
	 * external document id
	 */
	long uid;

	public Document() {
		super();
	}

	public String getAnalysisBatch() {
		return analysisBatch;
	}

	public byte[] getCas() {
		return cas;
	}

	public String getDocText() {
		return docText;
	}

	public List<DocumentAnnotation> getDocumentAnnotations() {
		return documentAnnotations;
	}

	public List<DocumentClass> getDocumentClasses() {
		return documentClasses;
	}

	public Integer getDocumentID() {
		return documentID;
	}

	public long getUid() {
		return uid;
	}

	public void setAnalysisBatch(String analysisBatch) {
		this.analysisBatch = analysisBatch;
	}

	public void setCas(byte[] cas) {
		this.cas = cas;
	}

	public void setDocText(String docText) {
		this.docText = docText;
	}

	public void setDocumentAnnotations(
			List<DocumentAnnotation> documentAnnotations) {
		this.documentAnnotations = documentAnnotations;
	}

	public void setDocumentClasses(List<DocumentClass> documentClasses) {
		this.documentClasses = documentClasses;
	}

	public void setDocumentID(Integer documentID) {
		this.documentID = documentID;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName() + " [documentID="
				+ documentID + ", documentAnnotations=" + documentAnnotations
				+ "]";
	}
}
