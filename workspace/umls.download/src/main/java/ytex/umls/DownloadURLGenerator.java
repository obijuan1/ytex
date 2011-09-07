package ytex.umls;

import org.jets3t.service.CloudFrontServiceException;

public interface DownloadURLGenerator {

	/**
	 * return the url
	 * @param version
	 * @param platform
	 * @return
	 * @throws CloudFrontServiceException
	 */
	public abstract String getDownloadURL(String version, String platform)
			throws CloudFrontServiceException;

}