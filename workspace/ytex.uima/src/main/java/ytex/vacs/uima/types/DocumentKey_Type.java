
/* First created by JCasGen Wed Jul 13 22:12:00 EDT 2011 */
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

/** 
 * Updated by JCasGen Wed Jul 13 22:12:58 EDT 2011
 * @generated */
public class DocumentKey_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocumentKey_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocumentKey_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocumentKey(addr, DocumentKey_Type.this);
  			   DocumentKey_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocumentKey(addr, DocumentKey_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DocumentKey.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ytex.vacs.uima.types.DocumentKey");
 
  /** @generated */
  final Feature casFeat_studyID;
  /** @generated */
  final int     casFeatCode_studyID;
  /** @generated */ 
  public int getStudyID(int addr) {
        if (featOkTst && casFeat_studyID == null)
      jcas.throwFeatMissing("studyID", "ytex.vacs.uima.types.DocumentKey");
    return ll_cas.ll_getIntValue(addr, casFeatCode_studyID);
  }
  /** @generated */    
  public void setStudyID(int addr, int v) {
        if (featOkTst && casFeat_studyID == null)
      jcas.throwFeatMissing("studyID", "ytex.vacs.uima.types.DocumentKey");
    ll_cas.ll_setIntValue(addr, casFeatCode_studyID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_uid;
  /** @generated */
  final int     casFeatCode_uid;
  /** @generated */ 
  public long getUid(int addr) {
        if (featOkTst && casFeat_uid == null)
      jcas.throwFeatMissing("uid", "ytex.vacs.uima.types.DocumentKey");
    return ll_cas.ll_getLongValue(addr, casFeatCode_uid);
  }
  /** @generated */    
  public void setUid(int addr, long v) {
        if (featOkTst && casFeat_uid == null)
      jcas.throwFeatMissing("uid", "ytex.vacs.uima.types.DocumentKey");
    ll_cas.ll_setLongValue(addr, casFeatCode_uid, v);}
    
  
 
  /** @generated */
  final Feature casFeat_documentType;
  /** @generated */
  final int     casFeatCode_documentType;
  /** @generated */ 
  public int getDocumentType(int addr) {
        if (featOkTst && casFeat_documentType == null)
      jcas.throwFeatMissing("documentType", "ytex.vacs.uima.types.DocumentKey");
    return ll_cas.ll_getIntValue(addr, casFeatCode_documentType);
  }
  /** @generated */    
  public void setDocumentType(int addr, int v) {
        if (featOkTst && casFeat_documentType == null)
      jcas.throwFeatMissing("documentType", "ytex.vacs.uima.types.DocumentKey");
    ll_cas.ll_setIntValue(addr, casFeatCode_documentType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_siteID;
  /** @generated */
  final int     casFeatCode_siteID;
  /** @generated */ 
  public String getSiteID(int addr) {
        if (featOkTst && casFeat_siteID == null)
      jcas.throwFeatMissing("siteID", "ytex.vacs.uima.types.DocumentKey");
    return ll_cas.ll_getStringValue(addr, casFeatCode_siteID);
  }
  /** @generated */    
  public void setSiteID(int addr, String v) {
        if (featOkTst && casFeat_siteID == null)
      jcas.throwFeatMissing("siteID", "ytex.vacs.uima.types.DocumentKey");
    ll_cas.ll_setStringValue(addr, casFeatCode_siteID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocumentKey_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_studyID = jcas.getRequiredFeatureDE(casType, "studyID", "uima.cas.Integer", featOkTst);
    casFeatCode_studyID  = (null == casFeat_studyID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_studyID).getCode();

 
    casFeat_uid = jcas.getRequiredFeatureDE(casType, "uid", "uima.cas.Long", featOkTst);
    casFeatCode_uid  = (null == casFeat_uid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uid).getCode();

 
    casFeat_documentType = jcas.getRequiredFeatureDE(casType, "documentType", "uima.cas.Integer", featOkTst);
    casFeatCode_documentType  = (null == casFeat_documentType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentType).getCode();

 
    casFeat_siteID = jcas.getRequiredFeatureDE(casType, "siteID", "uima.cas.String", featOkTst);
    casFeatCode_siteID  = (null == casFeat_siteID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_siteID).getCode();

  }
}



    