package controllers.webservices

import play.api.mvc.Controller
import util.Secured
import model.Upload
import org.squeryl._
import model.SquerylEntryPoint._
import model.AccountMember
import play.api.libs.json._
import model.dao.UploadSearch
import model.dao.UploadFindCriterion
import model.dao.UploadSortParam
import model.TransferStatus
import play.api.mvc.Request
import play.api.mvc.AnyContent
import java.util.Date
import java.sql.Timestamp


object UploadedFileStore extends Controller with Secured with DojoStoreParser {

  implicit val uploadWrite: Writes[Upload] = new Writes[Upload] {
    def writes(value: Upload): JsValue = {
      //Json.toJson(value).
      Json.obj(
          "guid" -> value.guid,
          "start_time" -> value.start_time,
          "end_time" -> value.end_time,
          "confirmed_download_count" -> value.confirmed_download_count,
          "status" -> TransferStatus(value.transfer_status_id.toInt).toString,
          "filename" -> value.filename,
          "description" -> value.description)
    }
  }

  implicit val uploadStoreWrite: Writes[(Upload, AccountMember)] = new Writes[(Upload, AccountMember)] {
    def writes(value: (Upload, AccountMember)): JsValue = {
      //Json.toJson(value).
      Json.obj(
        "guid" -> value._1.guid,
        "start_time" -> value._1.start_time,
        "end_time" -> value._1.end_time,
        "confirmed_download_count" -> value._1.confirmed_download_count,
        "status" -> TransferStatus(value._1.transfer_status_id.toInt).toString,
        "filename" -> value._1.filename,
        "description" -> value._1.description,
        "username" -> value._2.username)
    }
  }


  
  def buildFindCriteria(dojoRangeOptions: DojoRange, dojoSortOptions: DojoSort): UploadFindCriterion = {
    val sortString = dojoSortOptions.field.getOrElse("start_time")
    val sortParam = UploadSortParam.values.map(_.toString()).find(_ == sortString).map(UploadSortParam.withName(_)).getOrElse(UploadSortParam.start_time)
    new UploadFindCriterion(dojoRangeOptions.firstResult, dojoRangeOptions.numResults, sortParam, dojoSortOptions.descending)
  }
  
  def buildSearchCriteria(request: Request[AnyContent]) = {
    val startDate: Option[Timestamp] = request.getQueryString("startDate").map(s => new Timestamp(s.toLong))
    val endDate: Option[Timestamp] = request.getQueryString("endDate").map(s => new Timestamp(s.toLong))
    val globalSearch: Option[String] = request.getQueryString("globalSearch")
    val status = request.getQueryString("status").getOrElse("")
    val transferStatusId: Option[Long] = TransferStatus.values.map(_.toString()).find(_ == status).map(TransferStatus.withName(_).id)
    val username: Option[String] = request.getQueryString("username")
    
    new UploadSearch(startDate, endDate, globalSearch, transferStatusId, username)
  }
  
  
  def getUploadedFiles = IsAuthenticated { username => implicit request =>
    val range = parseRange(request.headers.get("Range").getOrElse("items=0-24"))
    val sort = parseSort(request.rawQueryString)
    val findCriteria = buildFindCriteria(range, sort)
    val searchCriteria = buildSearchCriteria(request)
    
    println(searchCriteria)
    
    val searchResults = transaction {
      val accountMember = AccountMember.get_username(username).head
      //val totalUploads = Upload.countUploadsForAccountMemberId(accountMember.id, searchCriteria)
      //val uploads = Upload.findUploadsForAccountMemberId(accountMember.id, searchCriteria , findCriteria)
      val totalUploads = Upload.countUploadsForAccountId(accountMember.account_id, searchCriteria)
      val uploads = Upload.findUploadsForAccountId(accountMember.account_id, searchCriteria , findCriteria)


      (totalUploads, uploads)
    } 
    
    val contentRange = "items " + range.firstResult + "-" + range.lastResult + "/" + searchResults._1;
    
    Ok(Json.toJson(searchResults._2)).withHeaders(CONTENT_RANGE -> contentRange)
  }
}