package services;

import play.Logger;
import util.AmazonS3Interface;
import util.AmazonS3UploadStream;

public class AmazonS3UploadThread implements Runnable {

	private AmazonS3UploadStream uploadStream;
	private String filename;
	private Long filesize;
	
	AmazonS3UploadThread(AmazonS3UploadStream uploadStream, String filename, Long filesize) {
		this.uploadStream = uploadStream;
		this.filename = filename;
		this.filesize = filesize;
	}
	
	
	@Override
	public void run() {
		try
		{
			AmazonS3Interface upload = new AmazonS3Interface();
			upload.putObject(this.uploadStream, filename, filesize);
			uploadStream.setUploadFinish();
		} catch(Exception e) {
			Logger.error("Error while uploading to Amazon S3", e);
			uploadStream.setUploadError();
		}
	}
}
