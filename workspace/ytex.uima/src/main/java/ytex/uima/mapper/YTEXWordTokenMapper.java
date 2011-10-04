package ytex.uima.mapper;

import ytex.uima.types.WordToken;



public class YTEXWordTokenMapper extends
		AbstractDocumentAnnotationMapper<ytex.model.WordToken, WordToken> {

	YTEXWordTokenMapper() {
		super(ytex.model.WordToken.class, WordToken.class);
	}

}
