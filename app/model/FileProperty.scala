package model

import org.squeryl.KeyedEntity
import org.squeryl._
import model.SquerylEntryPoint._

object FilePropertyEnum extends Enumeration {
  type FilePropertyEnum = Value

  val DeleteAfterDownload  = Value(1, "DeleteAfterDownload")
  val MustBeAuthenticated = Value(2, "MustBeAuthenticated")
  val MustBeAccountMember = Value(3, "MustBeAccountMember")
  val BillDownloadToUploader = Value(4, "BillDownloadToUploader")
  val DeleteAfterNumberOfDownloads = Value(5, "DeleteAfterNumberOfDownloads")
  val DeleteAfterNumberOfDays = Value(6, "DeleteAfterNumberOfDays")
  val NotifyUploaderAfterDownload = Value(7, "NotifyUploaderAfterDownload")
  
}

case class FileProperties(DeleteAfterDownload: Boolean, MustBeAuthenticated: 
    Boolean, MustBeAccountMember: Boolean, BillDownloadToUploader: Boolean, DeleteAfterNumberOfDownloads: 
    Option[Int], DeleteAfterNumberOfDays: Option[Int], NotifyUploaderAfterDownload: Boolean)

class FileProperty(
    var id: Long,
    var name: String,
    var default_value: Option[String],
    var description: Option[String],
    var type_id: Long
    ) extends KeyedEntity[Long] {
  
}

object FileProperty {
  
  def toBooleanString(value: Boolean) : Option[String] = {
    if ( value ) {
      Option("true")
    }
    else {
      Option("false")
    }
  }
  
  def toOptionString(value: Option[Int]): Option[String] = {
    value match {
      case None => None
      case Some(x) => Option(x.toString)
    }
  }
  
  
  def getAll: List[FileProperty] = {
    from(Library.fileProperty)(s => where(1 === 1) select(s) orderBy(s.id asc)).toList
  }
}


