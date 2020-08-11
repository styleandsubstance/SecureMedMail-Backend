package services

import play.api.mvc.{BodyParser, RequestHeader}
import play.api.mvc.BodyParsers.parse
import parse.Multipart.PartHandler
import play.api.mvc.MultipartFormData.FilePart
import play.api.Logger
import play.api.libs.iteratee.{Cont, Done, Input, Iteratee}
import util.AmazonS3UploadStream
import org.squeryl._
import model.SquerylEntryPoint._
import model.AccountMember
import model.Library
import model.Upload
import java.util.Date
import model.TransferStatus
import java.sql.Timestamp
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import util.AmazonS3Actor
import util.UploadFile
import scala.concurrent.ExecutionContext.Implicits.global



case class StreamingSuccess(guid: String);
case class StreamingError(guid: String, errorMessage: String);

object StreamingBodyParser {

  def streamingBodyParser(streamConstructor: String => Option[AmazonS3UploadStream]) = BodyParser { request =>
    // Use Play's existing multipart parser from play.api.mvc.BodyParsers.
    // The RequestHeader object is wrapped here so it can be accessed in streamingFilePartHandler
    parse.multipartFormData(new StreamingBodyParser(streamConstructor).streamingFilePartHandler(request)).apply(request)
  }
}

class StreamingBodyParser(streamConstructor: String => Option[AmazonS3UploadStream]) {

  
  /** Custom implementation of a PartHandler, inspired by these Play mailing list threads:
   * https://groups.google.com/forum/#!searchin/play-framework/PartHandler/play-framework/WY548Je8VB0/dJkj3arlBigJ
   * https://groups.google.com/forum/#!searchin/play-framework/PartHandler/play-framework/n7yF6wNBL_s/wBPFHBBiKUwJ */
  def streamingFilePartHandler(request: RequestHeader): PartHandler[FilePart[Either[StreamingError, StreamingSuccess]]] = {
    
    parse.Multipart.handleFilePart {
      case parse.Multipart.FileInfo(partName, filename, contentType) =>
        // Reference to hold the error message
        var errorMsg: Option[StreamingError] = None

        //val filesize = request.headers.get("CONTENT-LENGTH").get.toLong
        val filesize = request.headers.get("File-Size").getOrElse("-1").toLong

        
        val uploadFile = transaction {
           val accountMember = AccountMember.get_username(request.session.get("username").get).head
           Library.upload.insert(new Upload(0, new Timestamp(new Date().getTime()), None, Upload.generateGuid, filename, 0, None, 
               accountMember.id, TransferStatus.Transferring.id, None, 0, 0, request.remoteAddress))
        }
        
        
          /* Create the output stream. If something goes wrong while trying to instantiate the output stream, assign the
             error message to the result reference, e.g. `result = Some(StreamingError("network error"))`
             and set the outputStream reference to `None`; the `Iteratee` will then do nothing and the error message will
             be passed to the `Action`. */
         val outputStream: Option[AmazonS3UploadStream] = try {
            streamConstructor(uploadFile.guid)
          } catch {
            case e: Exception => {
              Logger.error(e.getMessage)
              errorMsg = Some(StreamingError(uploadFile.guid, e.getMessage))
              None
            }
          }

        val amazonActor = Akka.system.actorOf(Props[AmazonS3Actor])
        amazonActor ! UploadFile(outputStream.get, uploadFile.guid, filesize)
          
        // The fold method that actually does the parsing of the multipart file part.
        // Type A is expected to be Option[OutputStream]
        def fold[E, A](state: A)(f: (A, E) => A): Iteratee[E, A] = {
          def step(s: A)(i: Input[E]): Iteratee[E, A] = i match {
            case Input.EOF => Done(s, Input.EOF)
            case Input.Empty => Cont[E, A](i => step(s)(i))
            case Input.El(e) => {
              val s1 = f(s, e)
              errorMsg match { // if an error occurred during output stream initialisation, set Iteratee to Done
                case Some(result) => Done(s, Input.EOF)
                case None => Cont[E, A](i => step(s1)(i))
              }
            }
          }
          (Cont[E, A](i => step(state)(i)))
        }
        fold[Array[Byte], Option[AmazonS3UploadStream]](outputStream) { (os, data) =>
          
          try {
            os foreach { 
              println("Adding data" + data.length)
              _.addData(data, data.length) 
            }
          }  catch {
            case e: Exception => {
              Logger.error("Error while adding data", e)
              //errorMsg = Some(StreamingError(uploadFile.guid, e.getMessage))
              //None
            }
          }
          
          os
        }.map { os =>
          try {
        	os foreach {
        	  println("Waiting for upload to finish")
         	  _.waitForUploadToFinish  
        	}
          } catch {
              case e: Exception => {
                Logger.error(s"Error while waiting for upload with guid ${uploadFile.guid} to finish", e)
                errorMsg = Some(StreamingError(uploadFile.guid, e.getMessage))
              }
          }
          
          errorMsg match {
            case Some(result) =>
              Logger.error(s"Streaming the file $filename failed: ${result.errorMessage}")
              
              transaction {
                  uploadFile.size = os.get.getTotalServed()
                  uploadFile.transfer_status_id = TransferStatus.Cancelled.id
            	  uploadFile.end_time = Option(new Timestamp(new Date().getTime()))
            	  Library.upload.update(uploadFile)
              }
              
              Left(result)

            case None =>
              Logger.info(s"$uploadFile.guid finished streaming.")
              Logger.debug("Actual data served: " + os.get.getTotalServed());
              transaction {
                  uploadFile.size = os.get.getTotalServed()
            	  uploadFile.transfer_status_id = TransferStatus.Complete.id
            	  uploadFile.end_time = Option(new Timestamp(new Date().getTime()))
            	  Library.upload.update(uploadFile)
              }
              
              Right(StreamingSuccess(uploadFile.guid))
          }
        }
    }
  }
}
