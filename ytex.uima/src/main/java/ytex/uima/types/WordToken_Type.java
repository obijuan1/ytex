
/* First created by JCasGen Mon Oct 03 21:17:26 EDT 2011 */
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

/** Negatable Word Token
 * Updated by JCasGen Mon Oct 03 21:17:26 EDT 2011
 * @generated */
public class WordToken_Type extends edu.mayo.bmi.uima.core.ae.type.WordToken_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (WordToken_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = WordToken_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new WordToken(addr, WordToken_Type.this);
  			   WordToken_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new WordToken(addr, WordToken_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = WordToken.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ytex.uima.types.WordToken");
 
  /** @generated */
  final Feature casFeat_negated;
  /** @generated */
  final int     casFeatCode_negated;
  /** @generated */ 
  public boolean getNegated(int addr) {
        if (featOkTst && casFeat_negated == null)
      jcas.throwFeatMissing("negated", "ytex.uima.types.WordToken");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_negated);
  }
  /** @generated */    
  public void setNegated(int addr, boolean v) {
        if (featOkTst && casFeat_negated == null)
      jcas.throwFeatMissing("negated", "ytex.uima.types.WordToken");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_negated, v);}
    
  
 
  /** @generated */
  final Feature casFeat_possible;
  /** @generated */
  final int     casFeatCode_possible;
  /** @generated */ 
  public boolean getPossible(int addr) {
        if (featOkTst && casFeat_possible == null)
      jcas.throwFeatMissing("possible", "ytex.uima.types.WordToken");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_possible);
  }
  /** @generated */    
  public void setPossible(int addr, boolean v) {
        if (featOkTst && casFeat_possible == null)
      jcas.throwFeatMissing("possible", "ytex.uima.types.WordToken");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_possible, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public WordToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_negated = jcas.getRequiredFeatureDE(casType, "negated", "uima.cas.Boolean", featOkTst);
    casFeatCode_negated  = (null == casFeat_negated) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_negated).getCode();

 
    casFeat_possible = jcas.getRequiredFeatureDE(casType, "possible", "uima.cas.Boolean", featOkTst);
    casFeatCode_possible  = (null == casFeat_possible) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_possible).getCode();

  }
}



    