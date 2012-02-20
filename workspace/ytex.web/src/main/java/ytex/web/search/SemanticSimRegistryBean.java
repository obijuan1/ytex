package ytex.web.search;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SemanticSimRegistryBean {
	String defaultConceptGraphName;
	SortedMap<String, String> semanticSimDescriptionMap;
	List<SemanticSimServiceBean> semanticSimServiceList;

	SortedMap<String, SemanticSimServiceBean> semanticSimServiceMap;

	public String getDefaultConceptGraphName() {
		return defaultConceptGraphName;
	}

	public SortedMap<String, String> getSemanticSimDescriptionMap() {
		return semanticSimDescriptionMap;
	}

	public List<SemanticSimServiceBean> getSemanticSimServiceList() {
		return semanticSimServiceList;
	}

	public SortedMap<String, SemanticSimServiceBean> getSemanticSimServiceMap() {
		return semanticSimServiceMap;
	}

	public void setSemanticSimServiceList(
			List<SemanticSimServiceBean> semanticSimServiceList) {
		this.semanticSimServiceList = semanticSimServiceList;
		if (semanticSimServiceList != null && semanticSimServiceList.size() > 0) {
			semanticSimServiceMap = new TreeMap<String, SemanticSimServiceBean>();
			semanticSimDescriptionMap = new TreeMap<String, String>();
			defaultConceptGraphName = semanticSimServiceList.get(0)
					.getConceptSimilarityService().getConceptGraphName();
			for (SemanticSimServiceBean s : semanticSimServiceList) {
				semanticSimServiceMap.put(s.getConceptSimilarityService()
						.getConceptGraphName(), s);
				semanticSimDescriptionMap.put(s.getDescription(), s
						.getConceptSimilarityService().getConceptGraphName());
			}
		}
	}

}
