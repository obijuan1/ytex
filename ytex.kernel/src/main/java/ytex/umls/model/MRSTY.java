package ytex.umls.model;

import java.io.Serializable;

public class MRSTY implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String cui;
	String tui;
	String stn;
	String sty;
	String atui;
	int cvf;
	public String getCui() {
		return cui;
	}
	public void setCui(String cui) {
		this.cui = cui;
	}
	public String getTui() {
		return tui;
	}
	public void setTui(String tui) {
		this.tui = tui;
	}
	public String getStn() {
		return stn;
	}
	public void setStn(String stn) {
		this.stn = stn;
	}
	public String getSty() {
		return sty;
	}
	public void setSty(String sty) {
		this.sty = sty;
	}
	public String getAtui() {
		return atui;
	}
	public void setAtui(String atui) {
		this.atui = atui;
	}
	public int getCvf() {
		return cvf;
	}
	public void setCvf(int cvf) {
		this.cvf = cvf;
	}
	@Override
	public String toString() {
		return "MRSTY [cui=" + cui + ", tui=" + tui + "]";
	}
	public MRSTY() {
		super();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cui == null) ? 0 : cui.hashCode());
		result = prime * result + ((tui == null) ? 0 : tui.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MRSTY other = (MRSTY) obj;
		if (cui == null) {
			if (other.cui != null)
				return false;
		} else if (!cui.equals(other.cui))
			return false;
		if (tui == null) {
			if (other.tui != null)
				return false;
		} else if (!tui.equals(other.tui))
			return false;
		return true;
	}
	
	
}
