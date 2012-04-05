package ytex.uima.mapper;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class AnnoMappingInfo {
	String annoClassName;
	SortedMap<String, FieldMappingInfo> mapField = new TreeMap<String, FieldMappingInfo>();
	String tableName;
	String sql;

	@Override
	public String toString() {
		return "AnnoMappingInfo [mapField=" + mapField + ", tableName="
				+ tableName + "]";
	}

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
		for (Map.Entry<String, FieldMappingInfo> e : this.mapField.entrySet()) {
			n.mapField.put(e.getKey(), e.getValue().deepCopy());
		}
		return n;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getAnnoClassName() {
		return annoClassName;
	}

	public SortedMap<String, FieldMappingInfo> getMapField() {
		return mapField;
	}

	public String getTableName() {
		return tableName;
	}

	public void setAnnoClassName(String annoClassName) {
		this.annoClassName = annoClassName;
	}

	public void setMapField(SortedMap<String, FieldMappingInfo> mapField) {
		this.mapField = mapField;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
