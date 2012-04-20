package ytex.uima.mapper;

import org.apache.commons.beanutils.Converter;

public class ColumnMappingInfo {
	private String annoFieldName;
	private String columnName;
	private Converter converter;
	private String jxpath;
	private int size;
	private Class<?> targetType;
	private String targetTypeName;

	public ColumnMappingInfo() {
	}

	public ColumnMappingInfo deepCopy() {
		ColumnMappingInfo n = new ColumnMappingInfo();
		n.annoFieldName = this.annoFieldName;
		n.converter = this.converter;
		n.columnName = this.columnName;
		n.targetType = this.targetType;
		n.targetTypeName = this.targetTypeName;
		return n;
	}

	public String getAnnoFieldName() {
		return annoFieldName;
	}

	public String getColumnName() {
		return columnName;
	}

	public Converter getConverter() {
		return converter;
	}

	public String getJxpath() {
		return jxpath;
	}

	public int getSize() {
		return size;
	}

	public Class<?> getTargetType() {
		return targetType;
	}

	public String getTargetTypeName() {
		return targetTypeName;
	}

	public void setAnnoFieldName(String annoFieldName) {
		this.annoFieldName = annoFieldName;
	}

	public void setColumnName(String tableFieldName) {
		this.columnName = tableFieldName;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public void setJxpath(String jxpath) {
		this.jxpath = jxpath;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setTargetTypeName(String targetTypeName) {
		this.targetTypeName = targetTypeName;
		try {
			this.targetType = Class.forName(targetTypeName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "ColumnMappingInfo [columnName=" + columnName + "]";
	}

}
