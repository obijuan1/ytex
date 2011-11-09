package ytex.uima.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanUtils;

import ytex.model.Document;
import ytex.model.NamedEntityAnnotation;
import ytex.model.OntologyConceptAnnotation;
import ytex.model.UmlsConceptAnnotation;
import edu.mayo.bmi.uima.core.ae.type.NamedEntity;
import edu.mayo.bmi.uima.core.ae.type.OntologyConcept;
import edu.mayo.bmi.uima.core.ae.type.UmlsConcept;

public class NamedEntityDocumentAnnotationMapper extends
		AbstractDocumentAnnotationMapper<NamedEntityAnnotation, NamedEntity> {
	private static final Log log = LogFactory
			.getLog(NamedEntityDocumentAnnotationMapper.class);

	NamedEntityDocumentAnnotationMapper() {
		super(NamedEntityAnnotation.class, NamedEntity.class);
	}

	public NamedEntityAnnotation mapAnnotation(Annotation annotation,
			Document doc, SessionFactory sessionFactory) {
		NamedEntity ne = (NamedEntity) annotation;
		NamedEntityAnnotation namedEntityAnno = super.mapAnnotation(ne, doc,
				sessionFactory);
		FSArray ontoConcepts = ne.getOntologyConceptArr();
		if (namedEntityAnno != null && ontoConcepts != null) {
			int size = ontoConcepts.size();
			for (int i = 0; i < size; ++i) {
				FeatureStructure fstruct = ontoConcepts.get(i);
				OntologyConceptAnnotation ontologyConceptAnno = null;
				if (fstruct instanceof UmlsConcept) {
					UmlsConceptAnnotation umlsAnno = new UmlsConceptAnnotation(
							namedEntityAnno);
					ontologyConceptAnno = umlsAnno;
				} else if (fstruct instanceof OntologyConcept) {
					ontologyConceptAnno = new OntologyConceptAnnotation(
							namedEntityAnno);
				} else {
					log.warn("unknown fstruct in ontologyConceptArray:"
							+ fstruct);
				}
				if (ontologyConceptAnno != null) {
					BeanUtils.copyProperties(fstruct, ontologyConceptAnno);
					namedEntityAnno.getOntologyConcepts().add(
							ontologyConceptAnno);
					sessionFactory.getCurrentSession()
							.save(ontologyConceptAnno);
				}
			}
		}
		return namedEntityAnno;
	}
}
