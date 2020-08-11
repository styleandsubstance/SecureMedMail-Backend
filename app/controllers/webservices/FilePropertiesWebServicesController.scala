package controllers.webservices

import util.Secured
import org.squeryl._
import model.SquerylEntryPoint._
import model.FilePropertiesProfile
import model.FilePropertiesProfileFilePropertyValue
import model.AccountMember
import play.api.libs.json._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.Logger
import model.Upload
import model.UploadFileProperty


object FilePropertiesWebServicesController extends Controller with Secured {

  
  def getAllUserProfiles = IsAuthenticated { username => implicit request =>
    
    val userProfiles = transaction {
      val profiles = FilePropertiesProfile.getAllProfilesForUser(
          AccountMember.get_username(username).head.id);
      profiles.foreach(p => p.profile_file_properties.foreach(property => property.file_property))
      profiles
    }
    
    Ok(Json.toJson(userProfiles))
  }
  
  
   def getProfilePropertyValuesByProfileName = IsAuthenticated { username => implicit request =>
      request.body.asJson.map { json =>
      	(json \ "profileName").asOpt[String].map { name =>
      	  Logger.debug("Got a request for profile with name: " + name);
      	  
      	  val profileFileProperties:List[FilePropertiesProfileFilePropertyValue] = transaction {
      	    val profile: Option[FilePropertiesProfile] = FilePropertiesProfile.getProfileByName(
      	        AccountMember.get_username(username).head.id, name);
      	    profile.get.profile_file_properties.foreach(f => f.file_property)
      	    profile.get.profile_file_properties
      	  }
      	  
      	  Ok(Json.toJson(profileFileProperties))
      	}.getOrElse {
      		BadRequest("Missing parameter [name]")
      	}
      }.getOrElse {
    	  BadRequest("Expecting Json data")
      }
   }
   
   def getFilePropertiesForUpload = IsAuthenticated { username => implicit request =>
     request.body.asJson.map { json =>
      	(json \ "guid").asOpt[String].map { guid =>
      		Logger.debug("User: " + username + " requested upload file properties for GUID: " + guid)
      		
      		val uploadFileProperties: Option[List[UploadFileProperty]] = transaction {
      		  val accountMember = AccountMember.get_username(username).head
      		  val upload = Upload.findByGuidForAccountMember(accountMember.id, guid)
      		  upload.get.upload_file_properties
      		  upload.map(u => {
      		    u.upload_file_properties.foreach(p => p.file_property)
      		    Option(u.upload_file_properties)
      		  }).getOrElse(None)
      		}
      		
      		uploadFileProperties.map { x =>
      		  println("Here 2")
   			  Ok(Json.toJson(x))
      		}.getOrElse {
      		  BadRequest("Unable to get file properites")
      		}
        }.getOrElse {
      		BadRequest("Missing parameter [guid]")
      	}
      }.getOrElse {
    	  BadRequest("Expecting Json data")
      }
   }
}
