package util;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;

import play.Play;

public class AmazonS3DownloadStream extends InputStream {

	public static int DOWNLOAD_SIZE = 256*1024;
	
	byte [] data = new byte[AmazonS3DownloadStream.DOWNLOAD_SIZE];
	int dataPosition = 0;
	int dataLen = 0;
	String bucketName;
	String keyName;
	AmazonS3Client s3Client = null;
	boolean hasBeenInitialized = false;
	
	
	
	Long currrentDataStart = new Long(0);
	Long currentDataEnd = new Long(0);
	Long totalFileSize = new Long(0);
	Long totalBytesServed = new Long(0);
	Long downloadId = new Long(0);
	
	
	@Override
	public void close() throws IOException {
		
		super.close();
		if ( hasBeenInitialized == true && totalFileSize.equals(totalBytesServed) ) {
			controllers.FileController.handleFileDownloadCompletion(downloadId);
		}
		else {
			System.out.println("Close called without all data streamed");
		}
	}
	
	
	
	@Override
	public int read() throws IOException {
		
		byte [] toRead = new byte[1];
		int numRead = read(toRead, 0, 1);
		if ( numRead != -1 ) {
			return toRead[0];
		}
		else {
			return -1;
		}
		
	}
	
	@Override 
	public int read(byte[] b, int off, int len) throws IOException {
		if ( dataPosition < dataLen) {
			int toCopy =  Math.min(dataLen - dataPosition, len);
			System.arraycopy(data, dataPosition, b, off, toCopy);
			dataPosition += toCopy;
			totalBytesServed += toCopy;
			return toCopy;
		}
		else if ( totalFileSize != currentDataEnd ) {
			getDataFromAmazonS3();
			int toCopy =  Math.min(dataLen - dataPosition, len);
			System.arraycopy(data, dataPosition, b, off, toCopy);
			dataPosition += toCopy;
			totalBytesServed += toCopy;
			return toCopy;
		}
		else {
			return -1;
		}
	}
	
	
	public void getDataFromAmazonS3() throws IOException {
		
		currrentDataStart = currentDataEnd;
		currentDataEnd = currrentDataStart + (DOWNLOAD_SIZE);
		if ( currentDataEnd > totalFileSize) {
			currentDataEnd = totalFileSize;
		}
		
		
		GetObjectRequest rangeObjectRequest = new GetObjectRequest(
				bucketName, keyName);;
		rangeObjectRequest.setRange(currrentDataStart, currentDataEnd - 1);
		
		S3Object objectPortion = s3Client.getObject(
				rangeObjectRequest);
		
		InputStream objectData = objectPortion.getObjectContent();
		
		boolean done = false;
		int offset = 0;
		
		while( done == false ) {
			
			int numBytesRead = objectData.read(data, offset, data.length - offset);
			if ( numBytesRead == -1 ) {
				done = true;
			}
			else {
				offset += numBytesRead;
			}
		}
		objectData.close();
		
		dataPosition = 0;
		dataLen = offset;

		
	}
	
	
	public void init(String keyName, Long fileSize, Long downloadId) {
		
		String bucketName =
				Play.application().configuration().getString("securemedmail.amazons3.default.bucketname");
		
		String accessKey = 
				Play.application().configuration().getString("securemedmail.amazons3.accesskey");

		String secretKey =
				Play.application().configuration().getString("securemedmail.amazons3.secretkey");

		BasicAWSCredentials yourAWSCredentials = new BasicAWSCredentials(
				accessKey, secretKey);

		s3Client = new AmazonS3Client(yourAWSCredentials);
		this.bucketName = bucketName;
		this.keyName = keyName;
		this.totalFileSize = fileSize;
		this.downloadId = downloadId;
		hasBeenInitialized = true;

	}
}
