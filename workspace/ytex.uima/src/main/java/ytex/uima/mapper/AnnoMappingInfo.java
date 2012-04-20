package ytex.uima.mapper;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class AnnoMappingInfo {
	String annoClassName;
	ColumnMappingInfo coveredTextColumn;
	SortedMap<String, ColumnMappingInfo> mapField = new TreeMap<String, ColumnMappingInfo>();
	String sql;
	String tableName;
	int uimaTypeId;
	String uimaTypeIdColumnName;

	public AnnoMappingInfo() {
	}

	/**
	 * copy values from other annoMappingInfo
	 * 
	 * @param o
	 */
	public AnnoMappingInfo deepCopy() {
		AnnoMappingInfo n = new AnnoMappingInfo();
		n.annoClassName = this.annoClassName;
		n.tableName = this.tableName;
		n.sql = this.sql;
		n.coveredTextColumn = this.coveredTextColumn != null ? this.coveredTextColumn
				.deepCopy() : null;
		for (Map.Entry<String, ColumnMappingInfo> e : this.mapField.entrySet()) {
			n.mapField.put(e.getKey(), e.getValue().deepCopy());
		}
		return n;
	}

	public String getAnnoClassName() {
		return annoClassName;
	}

	public ColumnMappingInfo getCoveredTextColumn() {
		return coveredTextColumn;
	}

	public SortedMap<String, ColumnMappingInfo> getMapField() {
		return mapField;
	}

	public String getSql() {
		return sql;
	}

	public String getTableName() {
		return tableName;
	}

	public int getUimaTypeId() {
		return uimaTypeId;
	}

	public String getUimaTypeIdColumnName() {
		return uimaTypeIdColumnName;
	}

	public void setAnnoClassName(String annoClassName) {
		this.annoClassName = annoClassName;
	}

	public void setCoveredTextColumn(ColumnMappingInfo coveredTextColumn) {
		this.coveredTextColumn = coveredTextColumn;
	}

	public void setMapField(SortedMap<String, ColumnMappingInfo> mapField) {
		this.mapField = mapField;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setUimaTypeId(int uimaTypeId) {
		this.uimaTypeId = uimaTypeId;
	}

	public void setUimaTypeIdColumnName(String uimaTypeIdColumnName) {
		this.uimaTypeIdColumnName = uimaTypeIdColumnName;
	}

	@Override
	public String toString() {
		return "AnnoMappingInfo [mapField=" + mapField + ", tableName="
				+ tableName + "]";
	}

}
