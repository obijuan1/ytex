package ytex.uima.mapper;

public class SourceDocumentInformationMapper
		extends
		AbstractDocumentAnnotationMapper<ytex.model.SourceDocumentInformation, org.apache.uima.examples.SourceDocumentInformation> {

	SourceDocumentInformationMapper() {
		super(ytex.model.SourceDocumentInformation.class,
				org.apache.uima.examples.SourceDocumentInformation.class);
	}

}
