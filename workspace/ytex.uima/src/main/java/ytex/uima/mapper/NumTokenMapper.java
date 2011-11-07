package ytex.uima.mapper;

import edu.mayo.bmi.uima.core.ae.type.NumToken;

public class NumTokenMapper extends
		AbstractDocumentAnnotationMapper<ytex.model.NumToken, NumToken> {

	NumTokenMapper() {
		super(ytex.model.NumToken.class, NumToken.class);
	}

}
