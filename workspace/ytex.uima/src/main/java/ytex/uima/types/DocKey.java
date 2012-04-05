

/* First created by JCasGen Wed Apr 04 22:02:31 EDT 2012 */
package ytex.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Apr 04 22:02:31 EDT 2012
 * XML source: src/main/java/ytex/uima/types/YTEXTypes.xml
 * @generated */
public class DocKey extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DocKey.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocKey() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocKey(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocKey(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DocKey(JCas jcas, int begin, int end) {
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
  //* Feature: keyValuePairs

  /** getter for keyValuePairs - gets 
   * @generated */
  public FSArray getKeyValuePairs() {
    if (DocKey_Type.featOkTst && ((DocKey_Type)jcasType).casFeat_keyValuePairs == null)
      jcasType.jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DocKey_Type)jcasType).casFeatCode_keyValuePairs)));}
    
  /** setter for keyValuePairs - sets  
   * @generated */
  public void setKeyValuePairs(FSArray v) {
    if (DocKey_Type.featOkTst && ((DocKey_Type)jcasType).casFeat_keyValuePairs == null)
      jcasType.jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    jcasType.ll_cas.ll_setRefValue(addr, ((DocKey_Type)jcasType).casFeatCode_keyValuePairs, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for keyValuePairs - gets an indexed value - 
   * @generated */
  public KeyValuePair getKeyValuePairs(int i) {
    if (DocKey_Type.featOkTst && ((DocKey_Type)jcasType).casFeat_keyValuePairs == null)
      jcasType.jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocKey_Type)jcasType).casFeatCode_keyValuePairs), i);
    return (KeyValuePair)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocKey_Type)jcasType).casFeatCode_keyValuePairs), i)));}

  /** indexed setter for keyValuePairs - sets an indexed value - 
   * @generated */
  public void setKeyValuePairs(int i, KeyValuePair v) { 
    if (DocKey_Type.featOkTst && ((DocKey_Type)jcasType).casFeat_keyValuePairs == null)
      jcasType.jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocKey_Type)jcasType).casFeatCode_keyValuePairs), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocKey_Type)jcasType).casFeatCode_keyValuePairs), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    