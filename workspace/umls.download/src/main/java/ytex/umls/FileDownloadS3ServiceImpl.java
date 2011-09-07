package ytex.umls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class FileDownloadS3ServiceImpl {
	Properties awsProperties = null;
	public Properties getAwsProperties() {
		return awsProperties;
	}

	public void setAwsProperties(Properties awsProperties) {
		this.awsProperties = awsProperties;
	}

	AmazonS3Client s3Client;

	/**
	 * get the file from s3 and write it to the output stream
	 * 
	 * @param os
	 * @param version
	 * @param platform
	 * @return true - ok, false - not ok
	 * @throws IOException
	 */
	public boolean sendFile(BufferedOutputStream os, String version,
			String platform) throws IOException {
		GetObjectRequest rq = new GetObjectRequest("ytex", version + "/"
				+ "umls-" + platform + ".zip");
		S3Object s3o = s3Client.getObject(rq);
		if (s3o != null) {
			BufferedInputStream is = null;
			try {
				is = new BufferedInputStream(s3o.getObjectContent());
				int b = -1;
				while ((b = is.read()) != -1) {
					os.write(b);
				}
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
			return true;
		}
		return false;
	}

	public void init() {
		s3Client = new AmazonS3Client(new BasicAWSCredentials(
				awsProperties.getProperty("accessKey"),
				awsProperties.getProperty("secretKey")));
	}

}
