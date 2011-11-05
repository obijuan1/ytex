package ytex.uima.mapper;

import java.util.HashSet;
import java.util.Set;

/**
 * semi-ugly hack to communicate options from the MapperService to the
 * individual Mappers. The individual mappers are non-spring beans and should
 * stay that way.
 * 
 * @author vijay
 * 
 */
public class MapperConfig {
	private static MapperConfig mcDefault = new MapperConfig(
			new HashSet<String>(0), 0);

	static ThreadLocal<MapperConfig> tlConfig = new ThreadLocal<MapperConfig>() {

		@Override
		protected MapperConfig initialValue() {
			return mcDefault;
		}

	};

	protected static MapperConfig getConfig() {
		return tlConfig.get();
	}

	protected static void setConfig(Set<String> typesStoreCoveredText,
			int coveredTextMaxLen) {
		tlConfig.set(new MapperConfig(typesStoreCoveredText, coveredTextMaxLen));
	}

	protected static void unsetConfig() {
		tlConfig.set(null);
	}
	private int coveredTextMaxLen;

	private Set<String> typesStoreCoveredText;

	public MapperConfig(Set<String> typesStoreCoveredText, int coveredTextMaxLen) {
		super();
		this.typesStoreCoveredText = typesStoreCoveredText;
		this.coveredTextMaxLen = coveredTextMaxLen;
	}

	public int getCoveredTextMaxLen() {
		return coveredTextMaxLen;
	}

	public Set<String> getTypesStoreCoveredText() {
		return typesStoreCoveredText;
	}
}
