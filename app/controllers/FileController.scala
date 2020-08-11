package controllers

import play.api.mvc._
import play.api.libs.iteratee.Enumerator
import util.{AmazonS3UploadStream, AmazonS3DownloadStream}
import services.StreamingBodyParser
import util.Secured
import play.api.data.Form
import play.api.data.Forms._
import org.squeryl._
import model.SquerylEntryPoint._
import model.Upload
import model.Library
import model.Download
import model.AccountMember
import model.TransferStatus
import java.sql.Timestamp
import java.util.Date
import play.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import util.DownloadFile
import util.DownloadFileStreamed
import util.FileDownloadActor
import util.BillingActor
import util.BillUpload
import model.FilePropertiesProfile
import model.FilePropertiesProfileFilePropertyValue
import play.api.libs.json._
import model.FileProperties
import model.UploadFileProperty
import model.FilePropertyEnum
import model.FileProperty
import scala.concurrent.ExecutionContext.Implicits.global


case class DownloadRequest(guid: String);
case class UploadRequest(filesize: Long, contentType: String);

case class UploadForm(description: Option[String], password_hash: String, file_properties: FileProperties)

object FileController extends Controller with Secured {

    val uploadFileForm = Form(
      mapping (
        "description" -> optional(text),
        "password_hash" -> text,
        "file_properties" -> mapping (
	        "DeleteAfterDownload" -> boolean,
	        "MustBeAuthenticated" -> boolean,
	        "MustBeAccountMember" -> boolean,
	        "BillDownloadToUploader" -> boolean,
	        "DeleteAfterNumberOfDownloads" -> optional(number(min=1)),
	        "DeleteAfterNumberOfDays" -> optional(number(min=1)),
	        "NotifyUploaderAfterDownload" -> boolean
        )(FileProperties.apply)(FileProperties.unapply)
      )(UploadForm.apply)(UploadForm.unapply)
    )
  
  
    val downloadFileForm = Form(
      mapping(
          "guid" -> nonEmptyText
       )(DownloadRequest.apply)(DownloadRequest.unapply)
          
      .verifying("Invalid GUID",result => result match {
        case (downloadRequest) => {
          transaction {
            Upload.findByGuid(downloadRequest.guid).nonEmpty
          }
        }
      })

    )
    
    val generateGuidForm: Form[UploadRequest] = Form(
      mapping(
    	"filesize" -> longNumber,
    	"contentType" -> nonEmptyText
      )(UploadRequest.apply)(UploadRequest.unapply)  
    )
  
  
  def upload = IsAuthenticated { username => implicit request =>
    //val defaultProfileAndValues = 
    val defaultProfileFileProperties = transaction {
      val defaultProfileFileProperties = FilePropertiesProfile.getDefaultProfile(username)
      defaultProfileFileProperties.profile_file_properties.foreach(f => f.file_property)
      defaultProfileFileProperties
    }
    
    defaultProfileFileProperties.profile_file_properties.foreach(p => println(p.file_property.name))
    Ok(views.html.uploadfile(getUserFilePropertyProfiles(username), defaultProfileFileProperties.profile_file_properties))
  }
  
  def download(guid: String) =  Action { implicit request => 
    downloadFileForm.bind(Map(("guid", guid))).fold(
    	errors => {
    	  println("In FileController.download ERRORS");
          println(errors);
          BadRequest(views.html.index("Errors!"));
    	},
    	downloadRequest => {
    		val upload: Upload = transaction {
    		  val upload = Upload.findByGuid(downloadRequest.guid).head
    		  upload.upload_file_properties
    		  upload
    		}
    		
    		val uploadFilePropertiesValidation : Option[String] = transaction {
    		  upload.verify_file_properties(request.session.get("username"))
    		} 
    		
    		uploadFilePropertiesValidation match {
    		  case Some(errorString) =>
    		    println("Error: " + errorString)
    		    BadRequest(errorString)
    		  case None => {
    		    val downloadedFile: Download = transaction {
		    	    val uploadedFile = Upload.findByGuid(downloadRequest.guid).head
		    	    val accountMemberId = request.session.get("username").map(AccountMember.get_username(_).headOption.map(y => y.id)).getOrElse(None)
		    	    
			        val downloadedFile = Library.download.insert(new Download(0, uploadedFile.id, new Timestamp(new Date().getTime()), None, accountMemberId, TransferStatus.Transferring.id, request.remoteAddress, false, false, Download.generateGuid, None))
			        downloadedFile.upload
			        downloadedFile
    		    }
	            
				val data = new AmazonS3DownloadStream;
				
				data.init(downloadedFile.upload.guid, downloadedFile.upload.size, downloadedFile.id);
				
				//val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(data)
				Result(
					header = ResponseHeader(OK, Map(
							CONTENT_LENGTH -> downloadedFile.upload.size.toString,
							CONTENT_TYPE -> play.api.libs.MimeTypes.forFileName(downloadedFile.upload.filename).getOrElse(play.api.http.ContentTypes.BINARY),
							CONTENT_DISPOSITION -> ("""attachment; filename="%s"""".format(downloadedFile.upload.filename)),
							"File-Name" -> downloadedFile.upload.filename,
							"GUID" -> downloadedFile.guid
							)) ,
							Enumerator.fromStream(data),
							HttpConnection.Close
				)
    		  }
    		}
    	}
    )
  }
  
  
    /** Higher-order function that accepts the unqualified name of the file to stream to and returns the output stream
    * for the new file. This example streams to a file, but streaming to AWS S3 is also possible */
  def streamConstructor(filename: String) = {
    Option(new AmazonS3UploadStream());
  }
  
  def putFile = IsAuthenticated(StreamingBodyParser.streamingBodyParser(streamConstructor)) { username => request =>

    val uploadSuccess = request.body.files.length > 0 && request.body.files(0).ref.isRight

    if (uploadSuccess) { // streaming succeeded
      val result = request.body.files(0).ref
      val guid = result.right.get.guid
      
      uploadFileForm.bindFromRequest(request.body.dataParts).fold(
	      errors => {
	        println("IN ERRORS...shit");
	        println(errors);
	      },
	      uploadFileForm => {
	    	  val upload: Upload = transaction {
	    		  val upload = Upload.findByGuid(guid).head
	    		  
	    		  upload.description = uploadFileForm.description
	    		  upload.password_hash = Option(uploadFileForm.password_hash)
	    		  
	    		  Library.upload.update(upload)

	    		  Library.uploadFilePropertyValue.insert(
	    				  new UploadFileProperty(0, upload.id, FilePropertyEnum.DeleteAfterDownload.id, 
	    						  FileProperty.toBooleanString(uploadFileForm.file_properties.DeleteAfterDownload)));

	    		  Library.uploadFilePropertyValue.insert(
	    				  new UploadFileProperty(0, upload.id, FilePropertyEnum.MustBeAuthenticated.id, 
	    						  FileProperty.toBooleanString(uploadFileForm.file_properties.MustBeAuthenticated)));

	    		  Library.uploadFilePropertyValue.insert(
	    				  new UploadFileProperty(0, upload.id, FilePropertyEnum.MustBeAccountMember.id, 
	    						  FileProperty.toBooleanString(uploadFileForm.file_properties.MustBeAccountMember)));

	    		  Library.uploadFilePropertyValue.insert(
	    				  new UploadFileProperty(0, upload.id, FilePropertyEnum.BillDownloadToUploader.id, 
	    						  FileProperty.toBooleanString(uploadFileForm.file_properties.BillDownloadToUploader)));

	    		  Library.uploadFilePropertyValue.insert(
	    				  new UploadFileProperty(0, upload.id, FilePropertyEnum.DeleteAfterNumberOfDownloads.id, 
	    						  FileProperty.toOptionString(uploadFileForm.file_properties.DeleteAfterNumberOfDownloads)));

	    		  Library.uploadFilePropertyValue.insert(
	    				  new UploadFileProperty(0, upload.id, FilePropertyEnum.DeleteAfterNumberOfDays.id, 
	    						  FileProperty.toOptionString(uploadFileForm.file_properties.DeleteAfterNumberOfDays)));

	    		  Library.uploadFilePropertyValue.insert(
	    				  new UploadFileProperty(0, upload.id, FilePropertyEnum.NotifyUploaderAfterDownload.id, 
	    						  FileProperty.toBooleanString(uploadFileForm.file_properties.NotifyUploaderAfterDownload)));
	    		  
	    		  upload
	    	  }
	    	  
	    	  Akka.system.actorOf(Props[BillingActor]) ! BillUpload(upload)
	    	  
	      }
      )
      
      Ok(s"File $guid successfully streamed.")
      	.withHeaders("GUID" -> guid)
     } else { // file streaming failed
       Logger.warn("File upload failed with error") 
       BadRequest(s"Upload failed")
    }
  }
  

  def getUserFilePropertyProfiles(username: String) : List[FilePropertiesProfile] = {
    transaction {
      FilePropertiesProfile.getAllProfilesForUser(
          AccountMember.get_username(username).head.id);
    }
  }
  
  def getProfileFileProperties(profile_id: Long) : List[FilePropertiesProfileFilePropertyValue] = {
    transaction {
      FilePropertiesProfileFilePropertyValue.getAllForProfileByProfileId(profile_id);
    }
  }

  def handleFileDownloadCompletion(id: Long) = {
	   val downloadActor = Akka.system.actorOf(Props[FileDownloadActor])
        downloadActor ! DownloadFileStreamed(id)
  }
}