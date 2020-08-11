package util

import akka.actor._
import model.SquerylEntryPoint._
import model.Download
import java.sql.Timestamp
import java.util.Date
import model.Library
import model.TransferStatus
import model.FileProperty
import model.FilePropertyEnum
import play.libs.Akka
import model.AccountMember
import play.api.Logger
import model.Upload


case class DownloadFile(id: Long)
case class DownloadFileStreamed(id: Long)


class FileDownloadActor extends Actor {
	def receive = {
	  case DownloadFile(id) => {
		  val downloadFile = transaction {
		    val downloadFile = Download.findById(id).headOption.get;
		    downloadFile.confirmed_download = true;
		    downloadFile.end_time = Option(new Timestamp(new Date().getTime()));
		    downloadFile.transfer_status_id = TransferStatus.Complete.id
		    
		    Library.download.update(downloadFile)
		    
		    Upload.incrementConfirmedDownloadCount(downloadFile.upload_id)
		    
		    downloadFile.upload
		    downloadFile.upload.upload_file_properties
		    downloadFile
		  }
		  
		  Logger.debug("Checking file properties for upload GUID: " + downloadFile.upload.guid)
		  
		  //check the file properties
		  val deleteAfterDownloadProperty = 
		    downloadFile.upload.upload_file_properties.find(
		        uploadFileProperty => uploadFileProperty.file_property_id == FilePropertyEnum.DeleteAfterDownload.id)

         Logger.debug("File property deleteAfterDownloadProperty: " 
             + deleteAfterDownloadProperty + " for upload GUID: " + downloadFile.upload.guid)		        
		 if ( deleteAfterDownloadProperty.isDefined && deleteAfterDownloadProperty.get.isTrue ) {
		   Logger.debug("File property deleteAfterDownloadProperty for upload GUID: " + downloadFile.upload.guid + " was true...deleting file") 
		   val amazonActor = Akka.system.actorOf(Props[AmazonS3Actor])
	       amazonActor ! ScheduleFileForDeletion(downloadFile.upload.guid)
		 }       
		        
		 val deleteAfterNumberOfDownloadsProperty = 
		    downloadFile.upload.upload_file_properties.find(
		        uploadFileProperty => uploadFileProperty.file_property_id == FilePropertyEnum.DeleteAfterNumberOfDownloads.id)        
		  
		 Logger.debug("File property deleteAfterNumberOfDownloadsProperty: " 
		     + deleteAfterNumberOfDownloadsProperty + " for upload GUID: " + downloadFile.upload.guid)       
		     
		 deleteAfterNumberOfDownloadsProperty.map(u => {
		     val numberOfDownloadsToDeleteAfter = u.toLong
		     
		     Logger.debug("The file with upload GUID: " + downloadFile.upload.guid + " should be deleted after: " 
		         + numberOfDownloadsToDeleteAfter + " confirmed downloads")

		     val numberOfTimesThatFileHasBeenDownloaded = transaction {
		       Download.countConfirmedDownloadsForUploadbyId(downloadFile.upload.id)
		     }
		     
		     Logger.debug("The file with upload GUID: " + downloadFile.upload.guid + " has been successfully downloaded: " 
		         + numberOfTimesThatFileHasBeenDownloaded + " times");
		     if ( numberOfDownloadsToDeleteAfter != 0 && 
		         numberOfTimesThatFileHasBeenDownloaded >= numberOfDownloadsToDeleteAfter  ) {
		       Logger.debug("The file with upload GUID: " + downloadFile.upload.guid + " is being deleted after: " 
		           + numberOfTimesThatFileHasBeenDownloaded + " confirmed downloads");
		       val amazonActor = Akka.system.actorOf(Props[AmazonS3Actor])
		       amazonActor ! ScheduleFileForDeletion(downloadFile.upload.guid)
		     }
		 })    
		     
		 val notifyUploaderAfterDownload = 
		    downloadFile.upload.upload_file_properties.find(
		        uploadFileProperty => uploadFileProperty.file_property_id == FilePropertyEnum.NotifyUploaderAfterDownload.id)      
		 
		 Logger.debug("File property notifyUploaderAfterDownload: " 
		     + notifyUploaderAfterDownload + " for upload GUID: " + downloadFile.upload.guid)
		        
		 if ( notifyUploaderAfterDownload.isDefined && notifyUploaderAfterDownload.get.isTrue ) {
		   val notificationActor = Akka.system.actorOf(Props[NotificationActor])
		   
		   val accountMemberEmailAddress: Option[String] = transaction {
		     val accountMember = AccountMember.findByAccountMemberId(downloadFile.upload.uploaded_by_member_id).headOption
		     accountMember.map(member => Option(member.email)).getOrElse(None)
		   }
		   
		   accountMemberEmailAddress.map(emailAddress => {
		       val subject: String = "File Download Notification";
		       val message: String = "The file with GUID: " + downloadFile.upload.guid + " has been downloaded";
		     
		       notificationActor ! EmailNotification(emailAddress, subject, message)
		   })
		 }         
	  }
	  case DownloadFileStreamed(id) => {
	    val downloadFile = transaction {
		    val downloadFile = Download.findById(id).headOption.get;
		    downloadFile.streaming_success = true;
		    downloadFile.streaming_end_time = Option(new Timestamp(new Date().getTime()));
		    
		    Library.download.update(downloadFile)
		    
		    downloadFile
		}
	    
	    Akka.system.actorOf(Props[BillingActor]) ! BillDownload(downloadFile)
	  }
	}
}