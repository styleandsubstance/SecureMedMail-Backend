package model

import java.sql.Timestamp
import org.squeryl._
import model.TransferStatus._
import org.squeryl.annotations.{Column}
import model.SquerylEntryPoint._
import scala.util.Random
import java.util.Date


class Download(
    var id: Long,
    var upload_id: Long,
    var start_time: Timestamp,
    var end_time: Option[Timestamp],
    var downloaded_by_member_id: Option[Long],
    var transfer_status_id: Long,
    var remote_ip_address: String,
    var streaming_success: Boolean,
    var confirmed_download: Boolean,
    var guid: String,
    var streaming_end_time: Option[Timestamp]
    ) extends KeyedEntity[Long] {

  
   lazy val upload: Upload = {
     Library.uploadToDownloads.right(this).head
   }
   
   //lazy val downloaded_by_member: Option[AccountMember] = {
     //downloaded_by_member_id.map(did => Option(AccountMember.findByAccountMemberId(did).head)).getOrElse(None)
   //  Library.accountMemberToDownloads.right(this).headOption
   //}
}

object Download {
  
  val random = new Random(new Date().getTime())
  
  def findById(id: Long): Iterable[Download] =  {
    from(Library.download)(d => where((d.id === id)) select(d))
  }
  
  def findByGuid(guid: String): Iterable[Download] =  {
    from(Library.download)(d => where((d.guid === guid)) select(d))
  }
  
  def findAllCurrentlyDownloadingFilesForUpload(upload_id: Long) : List[Download] = {
    from(Library.download)(d => 
      where(d.upload_id === upload_id 
          and (d.transfer_status_id === TransferStatus.Transferring.id 
              or d.transfer_status_id === TransferStatus.Initiated.id))
    select(d)).toList
  }
  
  def countAllCurrentlyDownloadingFilesForUpload(upload_id: Long) : Long = {
    from(Library.download)(d => 
      where(d.upload_id === upload_id 
          and (d.transfer_status_id === TransferStatus.Transferring.id 
              or d.transfer_status_id === TransferStatus.Initiated.id))
    compute(count))
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
  
  def countConfirmedDownloadsForUploadbyId(upload_id: Long): Long = {
    from(Library.download)(d => 
    	where(d.upload_id === upload_id
    	    and d.confirmed_download === true)
    	compute(countDistinct(d.id))
    )
  }
  
  def getDownloadedByAccountMember(accountMemberId: Option[Long]): Option[AccountMember] = {
    accountMemberId.map(did => Option(AccountMember.findByAccountMemberId(did).head)).getOrElse(None)
  }
  
}