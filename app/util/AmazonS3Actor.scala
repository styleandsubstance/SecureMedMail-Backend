package util


import akka.actor._
import model.SquerylEntryPoint._
import model.Upload
import model.TransferStatus
import model.Library
import play.Logger

case class UploadFile(uploadStream: AmazonS3UploadStream , filename: String, filesize: Long)
case class DeleteFileFromAmazon(filename: String)
case class ScheduleFileForDeletion(filename: String)

class AmazonS3Actor extends Actor {

   def receive = {
     case UploadFile(uploadStream, filename, filesize) => {
        try {
          val upload: AmazonS3Interface = new AmazonS3Interface();
    	  upload.putObject(uploadStream, filename, filesize);
    	  uploadStream.setUploadFinish();         
        } catch {
          case e: Exception => {
            println("Here")
            Logger.error("Error while waiting for upload to finish", e)
            uploadStream.setUploadError();
          }
        }
     }
     case DeleteFileFromAmazon(filename) => {
         Logger.info("Deleting: " + filename + " from Amazon")
       
    	 val amazonS3Interface = new AmazonS3Interface();
    	 amazonS3Interface.deleteObject(filename);
    	 
    	 transaction {
    	   	val upload = Upload.findByGuid(filename).head  	  
	        	  
	        upload.transfer_status_id = TransferStatus.Deleted.id
	        Library.upload.update(upload);
    	 }
     }
     case ScheduleFileForDeletion(filename) => {
       Logger.info("Scheduling : " + filename + " for deletion")
       
       transaction {
    	   	val upload = Upload.findByGuid(filename).head  	  
	        	  
	        upload.transfer_status_id = TransferStatus.Deleting.id
	        Library.upload.update(upload);
       }
     }
     
     case _ => {
      Logger.error("Unknown message type received in AmazonS3Actor")
     }
   }
}

