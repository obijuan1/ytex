package gov.nih.nlm.nls.metamap.uima;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Strings;

import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.uima.MetaMapAnnotator;

/**
 * add ability to configure metamap server host and port. Put in the same
 * package as the MetaMapAnnotator so that we can access protected fields.
 * 
 * @author vijay
 * 
 */
public class YTEXMetaMapAnnotator extends MetaMapAnnotator {
	public static final String MMSERVER_HOST_PARAMETER = "metamap_server_host";
	public static final String MMSERVER_PORT_PARAMETER = "metamap_server_port";

	int mmserverPort = 8066;
	String mmserverHost = "127.0.0.1";

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		String host = (String) aContext
				.getConfigParameterValue(MMSERVER_HOST_PARAMETER);
		if (!Strings.isNullOrEmpty(host))
			mmserverHost = host;
		Integer port = (Integer) aContext
				.getConfigParameterValue(MMSERVER_PORT_PARAMETER);
		if (port != null && port.intValue() > 0)
			mmserverPort = port;
		this.api = new MetaMapApiImpl(host, port);
		String metamapOptions = (String) aContext
				.getConfigParameterValue(METAMAP_OPTIONS_PARAMETER);
		if (!Strings.isNullOrEmpty(metamapOptions))
			this.api.setOptions(metamapOptions);
	}
}
