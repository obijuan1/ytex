package ytex.uima.mapper;

import java.util.Set;

import org.apache.uima.jcas.JCas;

public interface DocumentMapperService {

	/**
	 * Save Document and all mapped annotations.
	 * 
	 * @param jcas
	 * @param analysisBatch
	 *            optional
	 * @return document id
	 */
	public abstract Integer saveDocument(JCas jcas, String analysisBatch,
			boolean bStoreDocText, boolean bStoreCAS,
			boolean bInsertAnnotationContainmentLinks, Set<String> typesToIgnore);

}