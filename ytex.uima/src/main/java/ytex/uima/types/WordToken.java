

/* First created by JCasGen Mon Oct 03 21:17:26 EDT 2011 */
package ytex.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Negatable Word Token
 * Updated by JCasGen Mon Oct 03 21:17:26 EDT 2011
 * XML source: E:/projects/ytex/ytex.uima/src/main/java/ytex/uima/types/WordTokenTypeSystem.xml
 * @generated */
public class WordToken extends edu.mayo.bmi.uima.core.ae.type.WordToken {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(WordToken.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected WordToken() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public WordToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public WordToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public WordToken(JCas jcas, int begin, int end) {
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
  //* Feature: negated

  /** getter for negated - gets 
   * @generated */
  public boolean getNegated() {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_negated == null)
      jcasType.jcas.throwFeatMissing("negated", "ytex.uima.types.WordToken");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((WordToken_Type)jcasType).casFeatCode_negated);}
    
  /** setter for negated - sets  
   * @generated */
  public void setNegated(boolean v) {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_negated == null)
      jcasType.jcas.throwFeatMissing("negated", "ytex.uima.types.WordToken");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((WordToken_Type)jcasType).casFeatCode_negated, v);}    
   
    
  //*--------------*
  //* Feature: possible

  /** getter for possible - gets 
   * @generated */
  public boolean getPossible() {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_possible == null)
      jcasType.jcas.throwFeatMissing("possible", "ytex.uima.types.WordToken");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((WordToken_Type)jcasType).casFeatCode_possible);}
    
  /** setter for possible - sets  
   * @generated */
  public void setPossible(boolean v) {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_possible == null)
      jcasType.jcas.throwFeatMissing("possible", "ytex.uima.types.WordToken");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((WordToken_Type)jcasType).casFeatCode_possible, v);}    
  }

    