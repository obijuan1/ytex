package ytex.uima.mapper;

import ytex.model.SentenceAnnotation;
import edu.mayo.bmi.uima.core.sentence.type.Sentence;

public class SentenceAnnotationMapper extends
		AbstractDocumentAnnotationMapper<SentenceAnnotation, Sentence> {

	SentenceAnnotationMapper() {
		super(SentenceAnnotation.class, Sentence.class);
	}

}
