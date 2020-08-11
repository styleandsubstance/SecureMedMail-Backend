package model

import org.squeryl.KeyedEntity
import org.squeryl._
import model.SquerylEntryPoint._
import play.api.libs.json._

class FilePropertiesProfile(

  var id: Long, 
  var name: String, 
  var description: Option[String], 
  var account_member_id: Long, 
  var is_default_profile: Boolean) extends KeyedEntity[Long] {
  
  
  
  lazy val profile_file_properties: List[FilePropertiesProfileFilePropertyValue] = {
	Library.profileToFilePropertyValues.left(this).toList.sortBy(f => f.id)
  }
  
}


object FilePropertiesProfile {
  
   implicit val filePropertiesProfileWrite = new Writes[FilePropertiesProfile] {
    def writes(value: FilePropertiesProfile): JsValue = {
      Json.obj(
        "name" -> value.name,
        "description" -> value.description,
        "is_default_profile" -> value.is_default_profile,
        "properties" -> Json.toJson(value.profile_file_properties)
      )
    }
  }
  
  
  
  /*
  implicit val filePropertiesProfileReads = Json.reads[FilePropertiesProfile]
  
  def apply(id: Long, name: String, description: Option[String], account_member_id: Long, is_default_profile: Boolean): FilePropertiesProfile = {
    new FilePropertiesProfile(id, name, description, account_member_id, is_default_profile);
  }
  
  def unapply(filePropertiesProfile: FilePropertiesProfile): Option[(Long, String, Option[String], Long, Boolean)] = {
    Option(filePropertiesProfile.id, filePropertiesProfile.name, filePropertiesProfile.description, filePropertiesProfile.account_member_id, filePropertiesProfile.is_default_profile)
  }
  */
  
  def assoicateDefaultFileProperties(filePropertyProfile: FilePropertiesProfile) = {
    val defaultFileProperties: List[FileProperty] = FileProperty.getAll
    defaultFileProperties.foreach(fileProperty => {
      Library.filePropertiesProfileFilePropertyValue.insert(
          new FilePropertiesProfileFilePropertyValue(0, filePropertyProfile.id, fileProperty.id, fileProperty.default_value));
    })
  }
  
  
  def resetDefaultProfile(account_member_id: Long) = {
    getAllProfilesForUser(account_member_id).foreach(profile => {
      profile.is_default_profile = false;
      Library.filePropertiesProfile.update(profile);
    })
  }
  
  
  def getProfileByName(account_member_id: Long, name: String) : Option[FilePropertiesProfile] = {
    from(Library.filePropertiesProfile)(s => where(s.account_member_id === account_member_id and s.name === name) select(s)).headOption
  }
  
  def getAllProfilesForUser(account_member_id: Long): List[FilePropertiesProfile] = {
    from(Library.filePropertiesProfile)(s => where(s.account_member_id === account_member_id) select(s) orderBy(s.name asc)).toList
  }
  
  def getDefaultProfile(username: String) : FilePropertiesProfile = {
    from(Library.filePropertiesProfile, Library.accountMember)( (fpp, am) => where(fpp.account_member_id === am.id and am.username === username and fpp.is_default_profile === true) select(fpp)).single
  }
}