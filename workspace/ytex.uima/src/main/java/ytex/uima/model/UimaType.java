package ytex.uima.model;

import java.io.Serializable;

/**
 * Reference data.  Defines YTEX uimaTypeID, uimaTypeName (Class name), and tableName.
 * We need our own uima Type ID because the typeID field in the annotation is generated dynamically -
 * it can change.
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

	private String tableName;
	private int uimaTypeID;
	private String uimaTypeName;
	public UimaType() {
		super();
	}
	public UimaType(int uimaTypeID, String uimaTypeName) {
		super();
		this.uimaTypeID = uimaTypeID;
		this.uimaTypeName = uimaTypeName;
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
	public String getTableName() {
		return tableName;
	}
	public int getUimaTypeID() {
		return uimaTypeID;
	}
	public String getUimaTypeName() {
		return uimaTypeName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + uimaTypeID;
		return result;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public void setUimaTypeID(int uimaTypeID) {
		this.uimaTypeID = uimaTypeID;
	}
	public void setUimaTypeName(String uimaTypeName) {
		this.uimaTypeName = uimaTypeName;
	}
	@Override
	public String toString() {
		return "UimaType [uimaTypeName=" + uimaTypeName + "]";
	}
}
