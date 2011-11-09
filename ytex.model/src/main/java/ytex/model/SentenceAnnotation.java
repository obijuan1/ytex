package ytex.model;


/**
 * Mapped to cTAKES Sentence.
 * @author vijay
 * @see Sentence
 *
 */
public class SentenceAnnotation extends DocumentAnnotation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Integer sentenceNumber;
	public Integer getSentenceNumber() {
		return sentenceNumber;
	}
	public void setSentenceNumber(Integer sentenceNumber) {
		this.sentenceNumber = sentenceNumber;
	}
	
	public SentenceAnnotation() {
		super();
	}
	
	public SentenceAnnotation(UimaType uimaType, Document doc) {
		super(uimaType, doc);
	}
//	public SentenceAnnotation(Sentence sentence, UimaType uimaType, Document doc) {
//		super(sentence, uimaType, doc);
//		this.sentenceNumber = sentence.getSentenceNumber();
//	}
}
