package ytex.uima.mapper;

import org.apache.commons.beanutils.Converter;

public class FieldMappingInfo {
	private String annoFieldName;
	private Converter converter;
	private String tableFieldName;
	private Class<?> targetType;

	@Override
	public String toString() {
		return "FieldMappingInfo [tableFieldName=" + tableFieldName + "]";
	}

	private String targetTypeName;

	public FieldMappingInfo() {
	}

	public FieldMappingInfo deepCopy() {
		FieldMappingInfo n = new FieldMappingInfo();
		n.annoFieldName = this.annoFieldName;
		n.converter = this.converter;
		n.tableFieldName = this.tableFieldName;
		n.targetType = this.targetType;
		n.targetTypeName = this.targetTypeName;
		return n;
	}

	public String getAnnoFieldName() {
		return annoFieldName;
	}

	public Converter getConverter() {
		return converter;
	}

	public String getTableFieldName() {
		return tableFieldName;
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

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public void setTableFieldName(String tableFieldName) {
		this.tableFieldName = tableFieldName;
	}

	public void setTargetTypeName(String targetTypeName) {
		this.targetTypeName = targetTypeName;
		try {
			this.targetType = Class.forName(targetTypeName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
