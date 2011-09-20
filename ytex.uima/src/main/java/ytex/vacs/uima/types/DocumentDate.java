

/* First created by JCasGen Wed Apr 28 21:48:01 EDT 2010 */
package ytex.vacs.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Document date
 * Updated by JCasGen Wed Apr 28 21:48:01 EDT 2010
 * XML source: E:/projects/VA/ytex/src/ytex/vacs/uima/types/DocumentInfoTypeSystem.xml
 * @generated */
public class DocumentDate extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DocumentDate.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentDate() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocumentDate(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocumentDate(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DocumentDate(JCas jcas, int begin, int end) {
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

  /** getter for date - gets Java Date.getTime
   * @generated */
  public long getDate() {
    if (DocumentDate_Type.featOkTst && ((DocumentDate_Type)jcasType).casFeat_date == null)
      jcasType.jcas.throwFeatMissing("date", "ytex.vacs.uima.types.DocumentDate");
    return jcasType.ll_cas.ll_getLongValue(addr, ((DocumentDate_Type)jcasType).casFeatCode_date);}
    
  /** setter for date - sets Java Date.getTime 
   * @generated */
  public void setDate(long v) {
    if (DocumentDate_Type.featOkTst && ((DocumentDate_Type)jcasType).casFeat_date == null)
      jcasType.jcas.throwFeatMissing("date", "ytex.vacs.uima.types.DocumentDate");
    jcasType.ll_cas.ll_setLongValue(addr, ((DocumentDate_Type)jcasType).casFeatCode_date, v);}    
  }

    