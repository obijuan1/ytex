package ytex.web.search;

import java.util.Properties;

/**
 * JSF Bean that holds search configuration parameters.
 * @author vijay
 *
 */
public class SearchConfigBean {
	private Properties searchProperties;

	public Properties getSearchProperties() {
		return searchProperties;
	}

	public void setSearchProperties(Properties searchProperties) {
		this.searchProperties = searchProperties;
	} 
}
