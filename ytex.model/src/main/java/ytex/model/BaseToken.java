package ytex.model;

/**
 * Mapped to cTAKES BaseToken
 * @author vijay
 *
 */
public class BaseToken extends DocumentAnnotation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer tokenNumber;
	private String normalizedForm;
	private String partOfSpeech;

	public Integer getTokenNumber() {
		return tokenNumber;
	}

	public void setTokenNumber(Integer tokenNumber) {
		this.tokenNumber = tokenNumber;
	}

	public String getNormalizedForm() {
		return normalizedForm;
	}

	public void setNormalizedForm(String normalizedForm) {
		this.normalizedForm = normalizedForm;
	}

	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}

	public BaseToken() {
		super();
	}

	public BaseToken(UimaType uimaType, Document doc) {
		super(uimaType, doc);
	}

}
