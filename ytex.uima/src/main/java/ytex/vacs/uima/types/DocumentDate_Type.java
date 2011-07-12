
/* First created by JCasGen Wed Apr 28 21:48:01 EDT 2010 */
package ytex.vacs.uima.types;

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

/** Document date
 * Updated by JCasGen Wed Apr 28 21:48:01 EDT 2010
 * @generated */
public class DocumentDate_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocumentDate_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocumentDate_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocumentDate(addr, DocumentDate_Type.this);
  			   DocumentDate_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocumentDate(addr, DocumentDate_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DocumentDate.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ytex.vacs.uima.types.DocumentDate");
 
  /** @generated */
  final Feature casFeat_date;
  /** @generated */
  final int     casFeatCode_date;
  /** @generated */ 
  public long getDate(int addr) {
        if (featOkTst && casFeat_date == null)
      jcas.throwFeatMissing("date", "ytex.vacs.uima.types.DocumentDate");
    return ll_cas.ll_getLongValue(addr, casFeatCode_date);
  }
  /** @generated */    
  public void setDate(int addr, long v) {
        if (featOkTst && casFeat_date == null)
      jcas.throwFeatMissing("date", "ytex.vacs.uima.types.DocumentDate");
    ll_cas.ll_setLongValue(addr, casFeatCode_date, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocumentDate_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_date = jcas.getRequiredFeatureDE(casType, "date", "uima.cas.Long", featOkTst);
    casFeatCode_date  = (null == casFeat_date) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_date).getCode();

  }
}



    