package model

import java.sql.Timestamp
import java.util.Date
import org.squeryl._
//import model.SquerylEntryPoint._
import model.SquerylEntryPoint._
import org.squeryl.annotations.{Column}
import org.squeryl.dsl.ManyToOne
import org.squeryl.dsl.OneToMany
import model.TransferStatus._
import scala.util.Random
import java.util.Calendar
//import model.DatabaseFunctions._
import model.dao.UploadSortParam._
import org.squeryl.dsl.ast.ExpressionNode
import model.dao.UploadSortParam
import model.dao.UploadFindCriterion
import model.dao.UploadSearch
import org.squeryl.dsl.fsm.WhereState
import org.squeryl.dsl.fsm.Conditioned
import org.squeryl.dsl.ast.OrderByArg
import org.squeryl.dsl.ast.OrderByExpression

class Upload(
    var id: Long,
    var start_time: Timestamp,
    var end_time: Option[Timestamp],
    var guid: String,
    var filename: String,
    var size: Long,
    var description: Option[String],
    var uploaded_by_member_id: Long,
    var transfer_status_id: Long,
    var password_hash: Option[String],
    var confirmed_download_count: Long,
    var deletion_attempts: Long,
    var remote_ip_address: String
    ) extends KeyedEntity[Long] {

  
  lazy val uploaded_by_member: AccountMember = {
    AccountMember.findByAccountMemberId(uploaded_by_member_id).head
  }
  
  lazy val upload_file_properties: List[UploadFileProperty] = {
    Library.uploadToUploadFileProperties.left(this).toList
  }
  
  def verify_file_properties(username: Option[String]) : Option[String] = {
    //verify the file is not deleted
    if ( transfer_status_id == TransferStatus.Deleted.id || transfer_status_id == TransferStatus.Deleting.id) {
    	return Option("This file no longer exists")
    }
    
    //verify if the user must be authenticated
    if ( upload_file_properties.find(property => property.file_property_id == FilePropertyEnum.MustBeAuthenticated.id).get.isTrue
           && username.isEmpty) {
    	return Option("This file requires that a user be authenticated")
    }
    
    //verify if the user must be an account member
    if ( upload_file_properties.find(property => property.file_property_id == FilePropertyEnum.MustBeAccountMember.id).get.isTrue ) {
      if ( username.isEmpty ) {
        return Option("This file requires that a user be authenticated")
      }
      
      val accountMember: Option[AccountMember] = AccountMember.get_username(username.get).headOption
      
      if ( accountMember.isEmpty ) {
        return Option("Username in session is not associated with an account member")
      }

      val uploadAccountMember: Option[AccountMember] = AccountMember.findByAccountMemberId(uploaded_by_member_id).headOption
      
      if ( uploadAccountMember.isEmpty) {
        return Option("This file does not have an associated member")
      }
      
      if ( accountMember.get.account_id != uploadAccountMember.get.account_id) {
        return Option("You must be an account member to download this file")
      }

      if ( accountMember.get.archive_time.isDefined)
        return Option("This user has been archived.  Please contact your administrator")

      if ( accountMember.get.downloads_allowed == false)
        return Option("Downloads have been disabled for this user.  Please contact your administrator")

    }
    
    None
  }
  
}

object Upload {
  
  val NUM_MILLIS_IN_A_DAY: Long = 86400000
  
  val random = new Random(new Date().getTime())
  
  def findByGuid(guid: String): Iterable[Upload] =  {
    from(Library.upload)(u => where((u.guid === guid)) select(u))
  }
  
  def findByGuidForAccountMember(account_member_id: Long, guid: String) = {
    from(Library.upload)(u => where( (u.guid === guid) and (u.uploaded_by_member_id === account_member_id)) select(u)).headOption
  }
  
  def generateGuid: String = {
    val guid =  Seq.fill(8)(random.nextInt(26)).map( (x: Int) =>  ((x + 65).asInstanceOf[Char].toString)).mkString("")
    if ( findByGuid(guid).isEmpty) {
      guid
    }
    else {
      generateGuid
    }
  }
  
  def findAllUploadsThatShouldBeDeletedToday(todaysDate: Date) : List[Upload] = {
    val todayTimestamp = new Timestamp(todaysDate.getTime());
    
    from(Library.upload)((upload) =>
      where( (get_deletion_date_for_upload(upload.id) lte todayTimestamp)
          and (upload.transfer_status_id === TransferStatus.Complete.id)) 
      select(upload)).forUpdate.toList
  }
  
  
  def findAllUploadsThatMustBeDeleted() : List[Upload] =  {
    from(Library.upload)(upload => 
      where(upload.transfer_status_id === TransferStatus.Deleting.id) select(upload)).forUpdate.toList
  }
  
  
 
  def buildSortForUpload(upload: Upload, sort: UploadSortParam, descending: Boolean): ExpressionNode = {
    	val orderByArg: OrderByExpression = {
	    	if ( sort == UploadSortParam.end_time ) {
	    		new OrderByExpression(upload.end_time);
	    	}
	    	else if ( sort == UploadSortParam.start_time) {
	    		new OrderByExpression(upload.start_time);
	    	}
	    	else if ( sort == UploadSortParam.guid) {
	    		new OrderByExpression(upload.guid);
	    	}
	    	else if ( sort == UploadSortParam.confirmed_download_count) {
	    		new OrderByExpression(upload.confirmed_download_count);
	    	}
	    	else if ( sort == UploadSortParam.filename) {
	    		new OrderByExpression(upload.filename);
	    	}
	    	else {
	    		new OrderByExpression(upload.description);
	    	}
    	}
    	
    	if ( descending == true ) {
    	  orderByArg.inverse
    	}
    	else {
    	  orderByArg
    	}
  }
  
  def buildWhereClause(upload: Upload, account_member_id: Long, searchCriteria: UploadSearch) : WhereState[Conditioned] = {
    val startTimestamp = searchCriteria.startDate.getOrElse(new Timestamp(0))
    val globalSearch: Option[String] = searchCriteria.globalSearch.map(searchString => Option("%" + searchString.toLowerCase() + "%")).getOrElse(None)
    
    val whereClause = where( 
        (upload.uploaded_by_member_id === account_member_id) and
        ((lower(upload.description) like globalSearch) or (lower(upload.filename) like globalSearch) or (lower(upload.guid) like globalSearch)).inhibitWhen(globalSearch == None) and
        (upload.transfer_status_id === searchCriteria.transferStatusId).inhibitWhen(searchCriteria.transferStatusId == None) and
        (upload.start_time gte startTimestamp).inhibitWhen(searchCriteria.startDate == None) and
        (upload.end_time lte searchCriteria.endDate).inhibitWhen(searchCriteria.endDate == None)
    )
    whereClause
  }


  def buildWhereClauseNew(account_id: Long, upload: Upload, accountMember: AccountMember,searchCriteria: UploadSearch) : WhereState[Conditioned] = {
    val startTimestamp = searchCriteria.startDate.getOrElse(new Timestamp(0))
    val globalSearch: Option[String] = searchCriteria.globalSearch.map(searchString => Option("%" + searchString.toLowerCase() + "%")).getOrElse(None)
    val username: String = searchCriteria.username.getOrElse("")


    val whereClause = where(
      (accountMember.account_id === account_id) and
        ((lower(upload.description) like globalSearch)
          or (lower(upload.filename) like globalSearch) or (lower(upload.guid) like globalSearch)
          or (lower(accountMember.username) like globalSearch)).inhibitWhen(globalSearch == None) and
        (upload.transfer_status_id === searchCriteria.transferStatusId).inhibitWhen(searchCriteria.transferStatusId == None) and
        (upload.start_time gte startTimestamp).inhibitWhen(searchCriteria.startDate == None) and
        (upload.end_time lte searchCriteria.endDate).inhibitWhen(searchCriteria.endDate == None) and
        (accountMember.username === username).inhibitWhen(searchCriteria.username == None)
    )
    whereClause
  }


  def countUploadsForAccountMemberId(account_member_id: Long, searchCriteria: UploadSearch): Long = {
    from(Library.upload)(upload =>
      buildWhereClause(upload, account_member_id, searchCriteria).compute(countDistinct(upload.id)))
  } 
  
  def findUploadsForAccountMemberId(account_member_id: Long, searchCriteria: UploadSearch, findCriteria: UploadFindCriterion): List[Upload] = {
    from(Library.upload)(upload =>
      buildWhereClause(upload, account_member_id, searchCriteria).select(upload).orderBy(buildSortForUpload(upload, findCriteria.sort, findCriteria.descending))).page(findCriteria.offset, findCriteria.pageLength).toList
  }
  
  def incrementConfirmedDownloadCount(upload_id: Long) = {
    update(Library.upload)(upload => 
      where(upload.id === upload_id)
      set(upload.confirmed_download_count := upload.confirmed_download_count.~ + 1)
    )
  }
  
  def incrementDeletionAttempts(upload_id: Long) = {
    update(Library.upload)(upload => 
      where(upload.id === upload_id)
      set(upload.deletion_attempts := upload.deletion_attempts.~ + 1)
    )
  }


  def findUploadsForAccountId(account_id: Long, searchCriteria: UploadSearch, findCriteria: UploadFindCriterion): List[(Upload, AccountMember)]= {
    join(Library.upload, Library.accountMember)((upload, accountMember) =>
      buildWhereClauseNew(account_id, upload, accountMember, searchCriteria)
      select(upload, accountMember)
      orderBy(buildSortForUpload(upload, findCriteria.sort, findCriteria.descending))
      on(upload.uploaded_by_member_id === accountMember.id)
    ).page(findCriteria.offset, findCriteria.pageLength).toList
  }


  def countUploadsForAccountId(account_id: Long, searchCriteria: UploadSearch): Long = {
    join(Library.upload, Library.accountMember)((upload, accountMember) =>
      buildWhereClauseNew(account_id, upload, accountMember, searchCriteria)
      compute(countDistinct(upload.id))
      on(upload.uploaded_by_member_id === accountMember.id)
    )
  }


}