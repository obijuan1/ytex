

/* First created by JCasGen Tue Mar 13 23:12:07 EDT 2012 */
package ytex.uima.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Tue Mar 13 23:12:07 EDT 2012
 * XML source: E:/projects/ytex/ytex.uima/src/main/java/ytex/uima/types/OntologyConceptTypeSystem.xml
 * @generated */
public class OntologyConcept extends edu.mayo.bmi.uima.core.ae.type.OntologyConcept {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(OntologyConcept.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected OntologyConcept() {}
    
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
  private void readObject() {}
     
 
    
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

    