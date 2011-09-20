package ytex.uima.mapper;

import edu.mayo.bmi.uima.core.ae.type.BaseToken;

public class NumTokenMapper extends
		AbstractDocumentAnnotationMapper<ytex.model.BaseToken, BaseToken> {

	NumTokenMapper() {
		super(ytex.model.BaseToken.class, BaseToken.class);
	}

}
