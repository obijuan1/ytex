package ytex.model;

/**
 * Mapped to cTAKES NumToken
 * @author vijay
 *
 */
public class NumToken extends BaseToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer numType; 

	public Integer getNumType() {
		return numType;
	}

	public void setNumType(Integer numType) {
		this.numType = numType;
	}

	public NumToken() {
		super();
	}

	public NumToken(UimaType uimaType, Document doc) {
		super(uimaType, doc);
	}
	
	
}
