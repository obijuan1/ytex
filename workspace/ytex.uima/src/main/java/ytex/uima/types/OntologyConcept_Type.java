
/* First created by JCasGen Thu Apr 19 20:50:38 EDT 2012 */
package ytex.uima.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Thu Apr 19 20:50:38 EDT 2012
 * @generated */
public class OntologyConcept_Type extends edu.mayo.bmi.uima.core.type.OntologyConcept_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (OntologyConcept_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = OntologyConcept_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new OntologyConcept(addr, OntologyConcept_Type.this);
  			   OntologyConcept_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new OntologyConcept(addr, OntologyConcept_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = OntologyConcept.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ytex.uima.types.OntologyConcept");
 
  /** @generated */
  final Feature casFeat_score;
  /** @generated */
  final int     casFeatCode_score;
  /** @generated */ 
  public double getScore(int addr) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "ytex.uima.types.OntologyConcept");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_score);
  }
  /** @generated */    
  public void setScore(int addr, double v) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "ytex.uima.types.OntologyConcept");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_score, v);}
    
  
 
  /** @generated */
  final Feature casFeat_disambiguated;
  /** @generated */
  final int     casFeatCode_disambiguated;
  /** @generated */ 
  public boolean getDisambiguated(int addr) {
        if (featOkTst && casFeat_disambiguated == null)
      jcas.throwFeatMissing("disambiguated", "ytex.uima.types.OntologyConcept");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_disambiguated);
  }
  /** @generated */    
  public void setDisambiguated(int addr, boolean v) {
        if (featOkTst && casFeat_disambiguated == null)
      jcas.throwFeatMissing("disambiguated", "ytex.uima.types.OntologyConcept");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_disambiguated, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public OntologyConcept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_score = jcas.getRequiredFeatureDE(casType, "score", "uima.cas.Double", featOkTst);
    casFeatCode_score  = (null == casFeat_score) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_score).getCode();

 
    casFeat_disambiguated = jcas.getRequiredFeatureDE(casType, "disambiguated", "uima.cas.Boolean", featOkTst);
    casFeatCode_disambiguated  = (null == casFeat_disambiguated) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_disambiguated).getCode();

  }
}



    