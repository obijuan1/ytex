package ytex.model;

/**
 * Maps to org.apache.uima.examples.SourceDocumentInformation
 * @author vijay
 *
 */
public class SourceDocumentInformation extends DocumentAnnotation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Boolean lastSegment;
	Integer offsetInSource;
	Integer documentSize;
	String uri;
	
	public Boolean getLastSegment() {
		return lastSegment;
	}

	public void setLastSegment(Boolean lastSegment) {
		this.lastSegment = lastSegment;
	}

	public Integer getOffsetInSource() {
		return offsetInSource;
	}

	public void setOffsetInSource(Integer offsetInSource) {
		this.offsetInSource = offsetInSource;
	}

	public Integer getDocumentSize() {
		return documentSize;
	}

	public void setDocumentSize(Integer documentSize) {
		this.documentSize = documentSize;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public SourceDocumentInformation() {
		super();
	}

	public SourceDocumentInformation(UimaType uimaType, Document doc) {
		super(uimaType, doc);
	}
	
}
