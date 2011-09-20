package ytex.umls;

import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.utils.ServiceUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * create a signed url to access a private file from cloudfront
 * 
 * @author vijay
 * 
 */
public class CloudfrontDownloadURLGeneratorImpl implements
		DownloadURLGenerator, InitializingBean {
	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	private byte[] derPrivateKey = null;
	private String keyPairId = null;
	private String distributionDomain = null;
	private Properties awsProperties = null;

	public Properties getAwsProperties() {
		return awsProperties;
	}

	public void setAwsProperties(Properties awsCredentials) {
		this.awsProperties = awsCredentials;
	}

	private static final Log log = LogFactory
			.getLog(CloudfrontDownloadURLGeneratorImpl.class);

	/**
	 * load the key as byte array. set aws properties
	 */
	public void afterPropertiesSet() {
		keyPairId = awsProperties.getProperty("keyPairId");
		distributionDomain = awsProperties.getProperty("distributionDomain");
		InputStream is = null;
		try {
			is = CloudfrontDownloadURLGeneratorImpl.class
					.getResourceAsStream("/aws.der");
			derPrivateKey = ServiceUtils.readInputStreamToBytes(is);
		} catch (IOException ioe) {
			log.error("error loading key", ioe);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ytex.umls.DownloadURLGenerator#getDownloadURL(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getDownloadURL(String version, String platform)
			throws IOException {
		try {
			String s3ObjectKey = "umlsdownload/" + version + "/umls-"
					+ platform + ".zip";
			// url valid for 30 minutes
			Calendar dateLessThan = Calendar.getInstance();
			dateLessThan.add(Calendar.MINUTE, 30);
			String signedUrlCanned = CloudFrontService.signUrlCanned("http://"
					+ distributionDomain + "/" + s3ObjectKey, // Resource URL or
																// Path
					keyPairId, // Certificate identifier,
								// an active trusted signer for the distribution
					derPrivateKey, // DER Private key data
					dateLessThan.getTime() // DateLessThan
					);
			return signedUrlCanned;
		} catch (CloudFrontServiceException e) {
			log.error("", e);
			throw new IOException(e);
		}
	}

	/**
	 * for testing
	 * 
	 * @param args
	 * @throws IOException
	 * @throws CloudFrontServiceException
	 */
	public static void main(String args[]) throws IOException,
			CloudFrontServiceException {
		CloudfrontDownloadURLGeneratorImpl dg = new CloudfrontDownloadURLGeneratorImpl();
		Properties props = new Properties();
		props.load(dg.getClass().getResourceAsStream(
				"/AwsCredentials.properties"));
		dg.setAwsProperties(props);
		dg.afterPropertiesSet();
		System.out.println(dg.getDownloadURL("0.3", "mysql"));
	}
}
