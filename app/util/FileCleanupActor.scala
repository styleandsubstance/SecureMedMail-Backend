package util

import akka.actor.Actor
import java.util.Date
import play.Logger
import model.SquerylEntryPoint._
import model.Upload
import model.Download
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import model.TransferStatus
import model.Library

case class DailyUploadDeletionByDateTask();
case class FileDeletionTask();


class FileCleanupActor extends Actor {

  def receive = {
    case DailyUploadDeletionByDateTask => {
      val todaysDate = new Date();
      
      Logger.info("Doing DailyUploadDeletionByDateTask: " +  todaysDate.toString())
      
      transaction {
        //query out the file from the database that are have exeeded
        //on the system past their maximum number of days
        val uploadedFiles: List[Upload] = Upload.findAllUploadsThatShouldBeDeletedToday(todaysDate)
        uploadedFiles.foreach(upload => {
        	  val amazonActor = Akka.system.actorOf(Props[AmazonS3Actor])
        	  amazonActor ! ScheduleFileForDeletion(upload.guid)
        	}
        )
      }
    }
    
    case FileDeletionTask => {
      val todaysDate = new Date();
      Logger.info("Doing FileDeletionTask: " +  todaysDate.toString())
      
      transaction {
        val uploadedFiles: List[Upload] = Upload.findAllUploadsThatMustBeDeleted()
        
        uploadedFiles.foreach(upload => {
        
        	//make sure no one is still downloading this file
             val downloadingFilesCount = Download.countAllCurrentlyDownloadingFilesForUpload(upload.id)
	        	
        	if ( downloadingFilesCount > 0) {
        	  //check to see what the current deletion failure count is and warn if greater than 10 attempts
        	  Upload.incrementDeletionAttempts(upload.id)
        	  if ( upload.deletion_attempts > 10) {
        	    Logger.error("We have attempted to deleted upload with guid: " + upload.guid + " and id: " + upload.id 
        	        + " " + upload.deletion_attempts + " times, but there are still: " + downloadingFilesCount + " downloads in progress");
        	  }
        	}
        	else {
        	  val amazonActor = Akka.system.actorOf(Props[AmazonS3Actor])
        	  amazonActor ! DeleteFileFromAmazon(upload.guid)
        	}
        })
      }
    }
    
    case _ => {
      Logger.error("Unknown message type received in FileCleanupActor")
    }
  }
}