package ytex.model;

import java.io.Serializable;

/**
 * Reference data.  Defines YTEX uimaTypeID, uimaTypeName (Class name), and mapperName (Class Name).
 * We need our own uima Type ID because the typeID field in the annotation is generated dynamically -
 * it can change.  The mapper maps the uima type to a class.
 * Mapped to ref_uima_type.
 * 
 * @author vijay
 *
 */
public class UimaType implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uimaTypeID;
	private String uimaTypeName;
	private String mapperName;
	public int getUimaTypeID() {
		return uimaTypeID;
	}
	public void setUimaTypeID(int uimaTypeID) {
		this.uimaTypeID = uimaTypeID;
	}
	public String getUimaTypeName() {
		return uimaTypeName;
	}
	public void setUimaTypeName(String uimaTypeName) {
		this.uimaTypeName = uimaTypeName;
	}
	public void setMapperName(String mapperName) {
		this.mapperName = mapperName;
	}
	public String getMapperName() {
		return mapperName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + uimaTypeID;
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
		UimaType other = (UimaType) obj;
		if (uimaTypeID != other.uimaTypeID)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "UimaType [uimaTypeName=" + uimaTypeName + "]";
	}
	public UimaType() {
		super();
	}
	public UimaType(int uimaTypeID, String uimaTypeName, String mapperName) {
		super();
		this.uimaTypeID = uimaTypeID;
		this.uimaTypeName = uimaTypeName;
		this.mapperName = mapperName;
	}
}
