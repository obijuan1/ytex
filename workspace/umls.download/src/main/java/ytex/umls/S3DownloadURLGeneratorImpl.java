package ytex.umls;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.springframework.beans.factory.InitializingBean;

public class S3DownloadURLGeneratorImpl implements DownloadURLGenerator,
		InitializingBean {
	private Properties awsProperties = null;
	private S3Service s3Service = null;

	public Properties getAwsProperties() {
		return awsProperties;
	}

	public void setAwsProperties(Properties awsCredentials) {
		this.awsProperties = awsCredentials;
	}

	private static final Log log = LogFactory
			.getLog(S3DownloadURLGeneratorImpl.class);

	public void afterPropertiesSet() {
		String awsAccessKey = awsProperties.getProperty("accessKey");
		String awsSecretKey = awsProperties.getProperty("secretKey");
		AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey,
				awsSecretKey);
		try {
			s3Service = new RestS3Service(awsCredentials);
		} catch (S3ServiceException e) {
			log.error("s3 init", e);
		}
	}

	public String getDownloadURL(String version, String platform)
			throws IOException {
		try {
			Calendar dateLessThan = Calendar.getInstance();
			dateLessThan.add(Calendar.MINUTE, 30);
			return s3Service.createSignedGetUrl("ytex", "umlsdownload/"
					+ version + "/umls-" + platform + ".zip",
					dateLessThan.getTime());
		} catch (S3ServiceException e) {
			log.error("", e);
			throw new IOException(e);
		}
	}

	public static void main(String args[]) throws Exception {
		S3DownloadURLGeneratorImpl dg = new S3DownloadURLGeneratorImpl();
		Properties props = new Properties();
		props.load(dg.getClass().getResourceAsStream(
				"/AwsCredentials.properties"));
		dg.setAwsProperties(props);
		dg.afterPropertiesSet();
		System.out.println(dg.getDownloadURL("0.3", "mysql"));
	}

}
