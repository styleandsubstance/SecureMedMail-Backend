package controllers

import util.Secured
import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import org.squeryl._
import model.SquerylEntryPoint._
import model.FileProperty
import model.Library
import model.AccountMember
import model.FilePropertiesProfile
import model.FilePropertiesProfileFilePropertyValue
import model.FilePropertyEnum
import play.api.mvc.RequestHeader
import model.FileProperties


case class FilePropertiesProfileForm(name: String, description: Option[String], is_default_profile: Boolean, file_properties: FileProperties)

object FilePropertiesProfileController extends Controller with Secured {

  def newFilePropertiesProfileForm(implicit request: RequestHeader) = Form(
      mapping (
        "name" -> text,
        "description" -> optional(text),
        "is_default_profile" -> boolean,
        "file_properties" -> mapping (
	        "DeleteAfterDownload" -> boolean,
	        "MustBeAuthenticated" -> boolean,
	        "MustBeAccountMember" -> boolean,
	        "BillDownloadToUploader" -> boolean,
	        "DeleteAfterNumberOfDownloads" -> optional(number(min=1)),
	        "DeleteAfterNumberOfDays" -> optional(number(min=1)),
	        "NotifyUploaderAfterDownload" -> boolean
        )(FileProperties.apply)(FileProperties.unapply)
      )(FilePropertiesProfileForm.apply)(FilePropertiesProfileForm.unapply).verifying("Profile name must be unique", result => result match {
        case(filePropertiesProfileCase) => transaction {
          val account_member_id = AccountMember.get_username(request.session.get("username").get).head.id
          FilePropertiesProfile.getProfileByName(account_member_id, filePropertiesProfileCase.name).isEmpty
        }
      })
  )
  
  def newProfile = IsAuthenticated { username => implicit request =>
      
    val defaultFileProperties = transaction {
      FileProperty.getAll
    }
    
    Ok(views.html.NewFilePropertiesProfile(defaultFileProperties))
  }
  
  def createNewProfile = IsAuthenticated { username => implicit request =>

    newFilePropertiesProfileForm.bindFromRequest.fold(
      errors => {
        println("IN createNewAccount ERRORS");
        println(errors);
        BadRequest(views.html.accounthome());
      },
      validatedForm => {
        
        println(validatedForm)
        
        println(validatedForm.file_properties.MustBeAuthenticated)
        
        
        
        
        transaction {
          val account_member_id =  AccountMember.get_username(username).head.id
          
          if ( validatedForm.is_default_profile) {
        	  FilePropertiesProfile.resetDefaultProfile(account_member_id);
          }
          
          val newFilePropertiesProfile = Library.filePropertiesProfile.insert(
              new FilePropertiesProfile(0, validatedForm.name, validatedForm.description, account_member_id, validatedForm.is_default_profile));
          
          Library.filePropertiesProfileFilePropertyValue.insert(
              new FilePropertiesProfileFilePropertyValue(0, newFilePropertiesProfile.id, FilePropertyEnum.DeleteAfterDownload.id, 
                  FileProperty.toBooleanString(validatedForm.file_properties.DeleteAfterDownload)));
          
          Library.filePropertiesProfileFilePropertyValue.insert(
              new FilePropertiesProfileFilePropertyValue(0, newFilePropertiesProfile.id, FilePropertyEnum.MustBeAuthenticated.id, 
                  FileProperty.toBooleanString(validatedForm.file_properties.MustBeAuthenticated)));

          Library.filePropertiesProfileFilePropertyValue.insert(
              new FilePropertiesProfileFilePropertyValue(0, newFilePropertiesProfile.id, FilePropertyEnum.MustBeAccountMember.id, 
                  FileProperty.toBooleanString(validatedForm.file_properties.MustBeAccountMember)));
           
          Library.filePropertiesProfileFilePropertyValue.insert(
              new FilePropertiesProfileFilePropertyValue(0, newFilePropertiesProfile.id, FilePropertyEnum.BillDownloadToUploader.id, 
                  FileProperty.toBooleanString(validatedForm.file_properties.BillDownloadToUploader)));
          
          Library.filePropertiesProfileFilePropertyValue.insert(
              new FilePropertiesProfileFilePropertyValue(0, newFilePropertiesProfile.id, FilePropertyEnum.DeleteAfterNumberOfDownloads.id, 
                  FileProperty.toOptionString(validatedForm.file_properties.DeleteAfterNumberOfDownloads)));
          
          Library.filePropertiesProfileFilePropertyValue.insert(
              new FilePropertiesProfileFilePropertyValue(0, newFilePropertiesProfile.id, FilePropertyEnum.DeleteAfterNumberOfDays.id, 
                  FileProperty.toOptionString(validatedForm.file_properties.DeleteAfterNumberOfDays)));
          
          Library.filePropertiesProfileFilePropertyValue.insert(
              new FilePropertiesProfileFilePropertyValue(0, newFilePropertiesProfile.id, FilePropertyEnum.NotifyUploaderAfterDownload.id, 
                  FileProperty.toBooleanString(validatedForm.file_properties.NotifyUploaderAfterDownload)));
        }
        
        Ok(views.html.index("Hello"));
      }
   )
  }
}