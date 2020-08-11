package util;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.InputStream;

import play.Play;

public class AmazonS3Interface {
	
	AmazonS3Client s3Client = null;
	
	private void init() {
		String accessKey = 
				Play.application().configuration().getString("securemedmail.amazons3.accesskey");

		String secretKey =
				Play.application().configuration().getString("securemedmail.amazons3.secretkey");

		BasicAWSCredentials yourAWSCredentials = new BasicAWSCredentials(
				accessKey, secretKey);    

		s3Client = new AmazonS3Client(yourAWSCredentials);
	}
	
	 public void putObject(InputStream input, String filename, Long totalFileSize) {
		  ObjectMetadata metadata = null;
		  
		  if (totalFileSize > 0)
		  {
			  metadata = new ObjectMetadata();
		      System.out.println("Total File Size:" + totalFileSize);
			  metadata.setContentLength(totalFileSize);
		  }
				  
	      String defaultBucketName = 
	    		  Play.application().configuration().getString("securemedmail.amazons3.default.bucketname");
	      
	      System.out.println("Is mark supported: " + input.markSupported());
	      System.setProperty("com.amazonaws.sdk.s3.defaultStreamBufferSize", "" + 256*1024);
	      
	      init();
		  s3Client.putObject(defaultBucketName, filename, input, metadata);
	  }
	 
	 public void deleteObject(String filename) {
		 String defaultBucketName = 
				 Play.application().configuration().getString("securemedmail.amazons3.default.bucketname");
		 init();
		 s3Client.deleteObject(defaultBucketName, filename);
	 }
}
