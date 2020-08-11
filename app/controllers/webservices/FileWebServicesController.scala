package controllers.webservices

import _root_.util.{Secured, FileDownloadActor, DownloadFile}
import play.api.mvc._
import play.api.libs.json._
import play.Logger
import org.squeryl._
import model.SquerylEntryPoint._
import model.Upload
import model.FilePropertyEnum
import play.api.libs.concurrent.Akka
import play.api.libs.functional.syntax._
import play.api.Play.current
import akka.actor.Props
import model.Download
import model.TransferStatus



case class FileDownloadRequirements(mustBeAccountMember: Boolean, mustBeAuthenticated: Boolean, filename: String, fileSize: Long, isReady: Boolean, isDeleted: Boolean)
case class VerifyFilePasswordHashRequest(guid: String, password_hash: String)

object FileWebServicesController extends Controller with Secured  {

  implicit val fileDownloadRequirementsWrite = new Writes[FileDownloadRequirements] {
    def writes(value: FileDownloadRequirements): JsValue = {
      Json.obj(
        "mustBeAccountMember" -> value.mustBeAccountMember,
        "mustBeAuthenticated" -> value.mustBeAuthenticated,
        "filename" -> value.filename,
        "fileSize" -> value.fileSize,
        "isReady" -> value.isReady,
        "isDeleted" -> value.isDeleted
      )
    }
  }

  implicit val verifyFilePasswordHashRequestReads: Reads[VerifyFilePasswordHashRequest] = (
    (__ \ "guid").read[String] and
    (__ \ "password_hash").read[String]
  )(VerifyFilePasswordHashRequest)

  
  def getFileAuthenticationRequirements = Action { implicit request =>
    Logger.debug("User requested getFileAuthenticationRequirements")
    
    request.body.asJson.map { json =>
        Logger.debug(json.toString)
	  	(json \ "guid").asOpt[String].map { guid =>
	  	  Logger.debug("Got a request for file with guid: " + guid);
	  	  
	  	  val guidFileDownloadRequirementsOption: Option[FileDownloadRequirements] = transaction {
	  	    val upload = Upload.findByGuid(guid).headOption
	  	    upload match {
	  	      case Some(up) =>
	  	        val upload_file_properties = up.upload_file_properties
	  	        
	  	        val mustBeAuthenticated = upload_file_properties.find( property => property.file_property_id == FilePropertyEnum.MustBeAuthenticated.id ).map(_.isTrue).getOrElse(false)
	  	        val mustBeAccountMember = upload_file_properties.find( property => property.file_property_id == FilePropertyEnum.MustBeAccountMember.id ).map(_.isTrue).getOrElse(false)
	  	        val isReady = up.transfer_status_id == TransferStatus.Complete.id
	  	        val isDeleted = up.transfer_status_id == TransferStatus.Deleted.id || up.transfer_status_id == TransferStatus.Deleting.id
	  	        
	  	      	Option(FileDownloadRequirements(mustBeAccountMember, mustBeAuthenticated, up.filename, up.size, isReady, isDeleted))
	  	      case None =>
	  	        None
	  	    }
	  	  }
	  	  
	  	  guidFileDownloadRequirementsOption match {
	  	    case Some(guidFileDownloadRequirements) =>
	  	      Logger.debug("Successfully retrieved authentication requirements for GUID: " + guid + " " +  guidFileDownloadRequirements);
	  	      Ok(Json.toJson(guidFileDownloadRequirements)).withHeaders("Error" -> "False")
	  	    case None =>
	  	      Logger.error("No such GUID")
	  	      Ok("No such GUID").withHeaders("Error" -> "True")
	  	  }
	  	}.getOrElse {
	  		Logger.error("User requested getFileAuthenticationRequirements without a valid guid")
	  		BadRequest("Missing parameter [guid]")
	  	}
	}.getOrElse {
	    Logger.error("User requested getFileAuthenticationRequirements with bad JSON data")
		BadRequest("Expecting Json data")
	}
  }
  
  def verifyFilePasswordHash = Action { implicit request => 
  	
    Logger.debug("User requested verifyFilePasswordHash")
    request.body.asJson.map { json =>
        //json.asOpt[VerifyFilePasswordHashRequest]
        val guid = (json \ "guid").asOpt[String]
        val password_hash = (json \ "password_hash").asOpt[String]
        
        Logger.debug("Got a guid: " + guid + " and password_hash: " + password_hash);
      
	  	guid.map{ guid =>
	  	    Logger.debug("Got a verifyFilePasswordHash request for GUID: " + guid)
	  	    password_hash.map { password_hash =>
	  	    	Logger.debug("Checking upload with guid: " + guid + " with password_hash: " + password_hash)
	  	    	
	  	    	val uploadPasswordHashMatch: Option[Upload] = transaction {
	  	    	  val upload = Upload.findByGuid(guid).headOption
	  	    	  upload match {
	  	    	  	case Some(up) =>
	  	    	 		if ( up.password_hash.getOrElse("") == password_hash) {
	  	    	 			Option(up)
	  	    	 		}
	  	    	 		else {
	  	    	 			None
	  	    	 		}
	  	    	 	case None =>
	  	    	 		None
	  	    	  }
	  	    	}
	  	    	
	  	    	uploadPasswordHashMatch match {
	  	    	  case Some(upload) =>
	  	    	    Logger.debug("Successful password verification")
	  	    	    Ok.withHeaders("Error" -> "False")
	  	    	  case None =>
	  	    	    Logger.error("Password is incorrect")
	  	    	    Ok("Password is incorrect").withHeaders("Error" -> "True")
	  	    	}
	  	    }.getOrElse {
	  	        Logger.error("Missing parameter [password_hash]")
	  	    	BadRequest("Missing parameter [password_hash]")
	  	    }
	  	}.getOrElse {
	  	    Logger.error("Missing parameter [guid]")
	  		BadRequest("Missing parameter [guid]")
	  	}
	}.getOrElse {
	    Logger.error("Request is not JSON data")
		BadRequest("Expecting Json data")
	}
  }
  
  def confirmDownloadByGuid = Action { implicit request =>
    Logger.debug("User requested confirmDownloadByGuid")
    
    request.body.asJson.map { json =>
        Logger.debug(json.toString)
	  	(json \ "guid").asOpt[String].map { guid =>
	  	  Logger.debug("Got a confirmDownloadByGuid request for file with download guid: " + guid);
	  	  
	  	  val downloadFile : Option[Download] = transaction {
	  	    Download.findByGuid(guid).headOption
	  	  }
	  	  
	  	  downloadFile match {
	  	    case Some(download) => {
	  	      val downloadActor = Akka.system.actorOf(Props[FileDownloadActor])
              downloadActor ! DownloadFile(download.id)
              Ok.withHeaders("Error" -> "False")
	  	    }
	  	    case None => {
	  	      Ok.withHeaders("Error" -> "True")
	  	    }
	  	  }
        }.getOrElse {
	  		Logger.error("User requested confirmDownloadByGuid without a valid guid")
	  		BadRequest("Missing parameter [guid]")
	  	}
	}.getOrElse {
	    Logger.error("User requested confirmDownloadByGuid with bad JSON data")
		BadRequest("Expecting Json data")
	}
  }


  def authorizeUpload =  IsAuthenticated { username => implicit request =>

    //receive the user, file size, file type, desired file properties

    Ok.withHeaders("Error" -> "False")
  }

}
