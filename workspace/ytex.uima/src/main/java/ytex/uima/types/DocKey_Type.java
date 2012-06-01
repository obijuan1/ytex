
/* First created by JCasGen Thu May 31 11:19:55 EDT 2012 */
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
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu May 31 11:19:55 EDT 2012
 * @generated */
public class DocKey_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocKey_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocKey_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocKey(addr, DocKey_Type.this);
  			   DocKey_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocKey(addr, DocKey_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DocKey.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ytex.uima.types.DocKey");
 
  /** @generated */
  final Feature casFeat_keyValuePairs;
  /** @generated */
  final int     casFeatCode_keyValuePairs;
  /** @generated */ 
  public int getKeyValuePairs(int addr) {
        if (featOkTst && casFeat_keyValuePairs == null)
      jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    return ll_cas.ll_getRefValue(addr, casFeatCode_keyValuePairs);
  }
  /** @generated */    
  public void setKeyValuePairs(int addr, int v) {
        if (featOkTst && casFeat_keyValuePairs == null)
      jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    ll_cas.ll_setRefValue(addr, casFeatCode_keyValuePairs, v);}
    
   /** @generated */
  public int getKeyValuePairs(int addr, int i) {
        if (featOkTst && casFeat_keyValuePairs == null)
      jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_keyValuePairs), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_keyValuePairs), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_keyValuePairs), i);
  }
   
  /** @generated */ 
  public void setKeyValuePairs(int addr, int i, int v) {
        if (featOkTst && casFeat_keyValuePairs == null)
      jcas.throwFeatMissing("keyValuePairs", "ytex.uima.types.DocKey");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_keyValuePairs), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_keyValuePairs), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_keyValuePairs), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocKey_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_keyValuePairs = jcas.getRequiredFeatureDE(casType, "keyValuePairs", "uima.cas.FSArray", featOkTst);
    casFeatCode_keyValuePairs  = (null == casFeat_keyValuePairs) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_keyValuePairs).getCode();

  }
}



    