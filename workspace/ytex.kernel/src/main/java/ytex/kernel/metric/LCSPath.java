package ytex.kernel.metric;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "LCSPath")
public class LCSPath implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<String> concept1Path;

	private List<String> concept2Path;
	
	private String lcs;

	public LCSPath() {
		super();
	}

	@XmlAttribute 
	public List<String> getConcept1Path() {
		return concept1Path;
	}

	@XmlAttribute 
	public List<String> getConcept2Path() {
		return concept2Path;
	}

	@XmlAttribute
	public String getLcs() {
		return lcs;
	}

	public void setConcept1Path(List<String> concept1Path) {
		this.concept1Path = concept1Path;
	}

	public void setConcept2Path(List<String> concept2Path) {
		this.concept2Path = concept2Path;
	}

	public void setLcs(String lcs) {
		this.lcs = lcs;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		if (getConcept1Path() != null && this.getConcept1Path().size() > 0) {
			formatPath(b, "->", getConcept1Path().iterator());
			b.append("->*");
		}
		b.append(this.getLcs());
		if (getConcept2Path() != null && this.getConcept2Path().size() > 0) {
			b.append("*<-");
			formatPath(b, "<-", getConcept2Path().iterator());
		}
		return b.toString();

	}

	private void formatPath(StringBuilder b, String link,
			Iterator<String> pathIter) {
		while (pathIter.hasNext()) {
			b.append(pathIter.next());
			if (pathIter.hasNext()) {
				b.append(link);
			}
		}
	}
}
