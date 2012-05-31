

/* First created by JCasGen Wed May 30 20:52:02 EDT 2012 */
package ytex.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Wed May 30 20:52:02 EDT 2012
 * XML source: ../config/desc/ytex/uima/YTEXTypes.xml
 * @generated */
public class OntologyConcept extends edu.mayo.bmi.uima.core.type.refsem.OntologyConcept {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(OntologyConcept.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected OntologyConcept() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public OntologyConcept(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public OntologyConcept(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: score

  /** getter for score - gets 
   * @generated */
  public double getScore() {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "ytex.uima.types.OntologyConcept");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_score);}
    
  /** setter for score - sets  
   * @generated */
  public void setScore(double v) {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "ytex.uima.types.OntologyConcept");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_score, v);}    
   
    
  //*--------------*
  //* Feature: disambiguated

  /** getter for disambiguated - gets 
   * @generated */
  public boolean getDisambiguated() {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_disambiguated == null)
      jcasType.jcas.throwFeatMissing("disambiguated", "ytex.uima.types.OntologyConcept");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_disambiguated);}
    
  /** setter for disambiguated - sets  
   * @generated */
  public void setDisambiguated(boolean v) {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_disambiguated == null)
      jcasType.jcas.throwFeatMissing("disambiguated", "ytex.uima.types.OntologyConcept");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_disambiguated, v);}    
  }

    