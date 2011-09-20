package ytex.model;

/**
 * Mapped to cTAKES WordToken
 * @author vijay
 *
 */
public class WordToken extends BaseToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer capitalization;
	private Integer numPosition;
	private Integer suggestion;
	private String canonicalForm;
	



	public Integer getNumPosition() {
		return numPosition;
	}

	public void setNumPosition(Integer numPosition) {
		this.numPosition = numPosition;
	}

	public Integer getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(Integer suggestion) {
		this.suggestion = suggestion;
	}

	public String getCanonicalForm() {
		return canonicalForm;
	}

	public void setCanonicalForm(String canonicalForm) {
		this.canonicalForm = canonicalForm;
	}

	public WordToken() {
		// TODO Auto-generated constructor stub
	}

	public WordToken(UimaType uimaType, Document doc) {
		super(uimaType, doc);
		// TODO Auto-generated constructor stub
	}

	public void setCapitalization(Integer capitalization) {
		this.capitalization = capitalization;
	}

	public Integer getCapitalization() {
		return capitalization;
	}

}
