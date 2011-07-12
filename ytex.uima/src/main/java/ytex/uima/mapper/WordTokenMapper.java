package ytex.uima.mapper;

import edu.mayo.bmi.uima.core.ae.type.WordToken;

public class WordTokenMapper extends
		AbstractDocumentAnnotationMapper<ytex.model.WordToken, WordToken> {

	WordTokenMapper() {
		super(ytex.model.WordToken.class, WordToken.class);
	}

}
