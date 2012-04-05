

/* First created by JCasGen Wed Apr 04 22:02:31 EDT 2012 */
package ytex.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Apr 04 22:02:31 EDT 2012
 * XML source: src/main/java/ytex/uima/types/YTEXTypes.xml
 * @generated */
public class Date extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Date.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Date() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Date(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Date(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Date(JCas jcas, int begin, int end) {
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
  //* Feature: date

  /** getter for date - gets ISO 8601 Formatted Timestamp: 
yyyy-MM-dd'T'HH:mm:ssZ
   * @generated */
  public String getDate() {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_date == null)
      jcasType.jcas.throwFeatMissing("date", "ytex.uima.types.Date");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Date_Type)jcasType).casFeatCode_date);}
    
  /** setter for date - sets ISO 8601 Formatted Timestamp: 
yyyy-MM-dd'T'HH:mm:ssZ 
   * @generated */
  public void setDate(String v) {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_date == null)
      jcasType.jcas.throwFeatMissing("date", "ytex.uima.types.Date");
    jcasType.ll_cas.ll_setStringValue(addr, ((Date_Type)jcasType).casFeatCode_date, v);}    
  }

    