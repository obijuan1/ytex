package ytex.uima.mapper;

import edu.mayo.bmi.uima.core.ae.type.NumToken;

public class BaseTokenMapper extends
		AbstractDocumentAnnotationMapper<ytex.model.NumToken, NumToken> {

	BaseTokenMapper() {
		super(ytex.model.NumToken.class, NumToken.class);
	}

}
