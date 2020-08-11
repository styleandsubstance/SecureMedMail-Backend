package util;

import java.io.IOException;
import java.io.InputStream;

import play.Logger;

public class AmazonS3UploadStream extends InputStream {

	
	byte [] data = new byte[256 * 1024];
	
	int dataLen = 0;
	int dataPosition = 0;
	boolean fileUploadDone = false;
	boolean amazonUploadDone = false;
	boolean amazonUploadError = false;
	Long totalServed = new Long(0);
	
	@Override
	public int read() throws IOException {
		byte [] temp = new byte[1];
		return read(temp, 0, 1);
		
	}
	
	
	public int readData(byte[] b, int off, int len) {
		int numBytesToCopy = Math.min(dataLen - dataPosition, len);
		System.arraycopy(data, dataPosition, b, off, numBytesToCopy);
		dataPosition += numBytesToCopy;
		totalServed+= numBytesToCopy;
		return numBytesToCopy;
	}
	
	
	@Override 
	public int read(byte[] b, int off, int len) throws IOException {
		synchronized (data) {
			if ( dataPosition < dataLen ) {
				//we have data for them
				int toReturn = readData(b, off, len);
				data.notify();
				return toReturn;
			}
			else if ( fileUploadDone == true ) {
				return -1;
			}
			else {
				
				boolean loopDone = false;
				
				while ( loopDone == false ) {
					try {
						data.wait();

					} catch(Exception e) {
						e.printStackTrace();
						Logger.error("Unexpected error when reading data for AmazonS3UploadStream Buffer", e);
						throw new IOException(e.getMessage());
					}
					
					
					if ( dataPosition < dataLen ) {
						int toReturn = readData(b, off, len);
						data.notify();
						return toReturn;
					}
					else if ( fileUploadDone == true ) {
						data.notify();
						return -1;
					}
					else {
						//false positive...go back to waiting
						continue;
					}
				}
			}
		}
		return -1;
	}
	
	
	@Override
	public int available() throws IOException {
		synchronized(data) {
			if ( fileUploadDone == true ) {
				return 0;
			}
			else {
				return dataLen - dataPosition;
			}
		}
	}
	
	
	public void addData(byte[] toAdd, int len) {
		synchronized (data) {
			if ( dataPosition == dataLen ) {
				dataPosition = 0;
				dataLen = 0;
			}
			if ( dataLen + len > (256 * 1024)) {
				
				
				boolean loopDone = false;
				
				while ( loopDone == false ) {
					
					try {
						data.wait();
						
						//check to see if there is now enough space to copy data
						if ( dataPosition == dataLen) {
							dataPosition = 0;
							dataLen = 0;

							System.arraycopy(toAdd, 0, data, dataLen, len);
							dataLen += len;
							data.notify();
							return;
						}
					} catch(Exception e) {
						e.printStackTrace();
						Logger.error("Unexpected error when adding data to AmazonS3UploadStream Buffer", e);
					}
				}
			}
			else {
				System.arraycopy(toAdd, 0, data, dataLen, len);
				dataLen += len;
				data.notify();
				return;
			}
		}
	}
	
	public void setUploadFinish() {
		synchronized(this) {
			amazonUploadDone = true;
			this.notify();
		}
	}
	
	public void setUploadError() {
		synchronized(this) {
			amazonUploadError = true;
			this.notify();
		}
	}
	
	public void waitForUploadToFinish() throws IOException {
		
		synchronized (data) {
			fileUploadDone = true;
			data.notify();
		}
		
		
		synchronized(this) {
			if ( amazonUploadDone == true ) {
				return;
			}
			else {
				
				while(true) {
					try {
						this.wait();
						
						if ( amazonUploadDone == true ) {
							return;
						}
						
						if ( amazonUploadError == true ) {
							throw new IOException("Amazon S3 Upload Error");
						}
						
					} catch(Exception e) {
						throw new IOException( e.getMessage());
					}
				}
			}
		}
	}
	
	public Long getTotalServed() {
		return totalServed;
	}
}
