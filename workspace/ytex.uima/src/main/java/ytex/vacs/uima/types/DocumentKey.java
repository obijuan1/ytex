

/* First created by JCasGen Wed Jul 13 22:12:00 EDT 2011 */
package ytex.vacs.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Jul 13 22:12:58 EDT 2011
 * XML source: E:/projects/ytex/ytex.uima/src/main/java/ytex/vacs/uima/types/DocumentKeyTypeSystem.xml
 * @generated */
public class DocumentKey extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DocumentKey.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentKey() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocumentKey(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocumentKey(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DocumentKey(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: studyID

  /** getter for studyID - gets 
   * @generated */
  public int getStudyID() {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_studyID == null)
      jcasType.jcas.throwFeatMissing("studyID", "ytex.vacs.uima.types.DocumentKey");
    return jcasType.ll_cas.ll_getIntValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_studyID);}
    
  /** setter for studyID - sets  
   * @generated */
  public void setStudyID(int v) {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_studyID == null)
      jcasType.jcas.throwFeatMissing("studyID", "ytex.vacs.uima.types.DocumentKey");
    jcasType.ll_cas.ll_setIntValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_studyID, v);}    
   
    
  //*--------------*
  //* Feature: uid

  /** getter for uid - gets 
   * @generated */
  public long getUid() {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_uid == null)
      jcasType.jcas.throwFeatMissing("uid", "ytex.vacs.uima.types.DocumentKey");
    return jcasType.ll_cas.ll_getLongValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_uid);}
    
  /** setter for uid - sets  
   * @generated */
  public void setUid(long v) {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_uid == null)
      jcasType.jcas.throwFeatMissing("uid", "ytex.vacs.uima.types.DocumentKey");
    jcasType.ll_cas.ll_setLongValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_uid, v);}    
   
    
  //*--------------*
  //* Feature: documentType

  /** getter for documentType - gets 
   * @generated */
  public int getDocumentType() {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_documentType == null)
      jcasType.jcas.throwFeatMissing("documentType", "ytex.vacs.uima.types.DocumentKey");
    return jcasType.ll_cas.ll_getIntValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_documentType);}
    
  /** setter for documentType - sets  
   * @generated */
  public void setDocumentType(int v) {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_documentType == null)
      jcasType.jcas.throwFeatMissing("documentType", "ytex.vacs.uima.types.DocumentKey");
    jcasType.ll_cas.ll_setIntValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_documentType, v);}    
   
    
  //*--------------*
  //* Feature: siteID

  /** getter for siteID - gets Document Key
   * @generated */
  public String getSiteID() {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_siteID == null)
      jcasType.jcas.throwFeatMissing("siteID", "ytex.vacs.uima.types.DocumentKey");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_siteID);}
    
  /** setter for siteID - sets Document Key 
   * @generated */
  public void setSiteID(String v) {
    if (DocumentKey_Type.featOkTst && ((DocumentKey_Type)jcasType).casFeat_siteID == null)
      jcasType.jcas.throwFeatMissing("siteID", "ytex.vacs.uima.types.DocumentKey");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentKey_Type)jcasType).casFeatCode_siteID, v);}    
  }

    