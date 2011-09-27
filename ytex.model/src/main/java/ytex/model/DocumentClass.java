package ytex.model;

import java.io.Serializable;

/**
 * Mapped to document_class.
 * Used to store gold standard document class and predicted document class.
 * @author vijay
 *
 */
public class DocumentClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int documentClassID;
//	private int id;
	private String task;
	private Integer classAuto;
	private Integer classGold;
	private Document document;
	
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public int getDocumentClassID() {
		return documentClassID;
	}
	public void setDocumentClassID(int documentClassID) {
		this.documentClassID = documentClassID;
	}
//	public int getId() {
//		return id;
//	}
//	public void setId(int id) {
//		this.id = id;
//	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public Integer getClassAuto() {
		return classAuto;
	}
	public void setClassAuto(Integer classAuto) {
		this.classAuto = classAuto;
	}
	public Integer getClassGold() {
		return classGold;
	}
	public void setClassGold(Integer classGold) {
		this.classGold = classGold;
	}
	public DocumentClass() {
		super();
	}
}
