package model

import org.squeryl.KeyedEntity
import model.SquerylEntryPoint._
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsValue

class FilePropertiesProfileFilePropertyValue(
    var id: Long,
    var profile_id: Long, 
    var file_property_id: Long,
    var file_property_value: Option[String]) extends KeyedEntity[Long] {

  lazy val file_property: FileProperty = {
    Library.fileProperty.get(file_property_id)
  }  
  
}


object FilePropertiesProfileFilePropertyValue {
  
  implicit val filePropertiesProfileFilePropertyValueWrite = new Writes[FilePropertiesProfileFilePropertyValue] {
    def writes(value: FilePropertiesProfileFilePropertyValue): JsValue = {
      Json.obj(
        "name" -> value.file_property.name,
        "type" -> FilePropertyType(value.file_property.type_id.toInt).toString(),
        "value" -> value.file_property_value
      )
    }
  }
  
  
  def apply(id: Long, profile_id: Long, file_property_id: Long, file_property_value: Option[String]): FilePropertiesProfileFilePropertyValue = {
    new FilePropertiesProfileFilePropertyValue(id, profile_id, file_property_id, file_property_value);
  }
  
  def unapply(filePropertiesProfileFilePropertyValue: FilePropertiesProfileFilePropertyValue): Option[(Long, Long, Long, Option[String])] = {
    Option(filePropertiesProfileFilePropertyValue.id, filePropertiesProfileFilePropertyValue.profile_id, 
        filePropertiesProfileFilePropertyValue.file_property_id, filePropertiesProfileFilePropertyValue.file_property_value)
  }
  
  def getAllForProfileByProfileId(profile_id: Long) : List[FilePropertiesProfileFilePropertyValue] = {
    from(Library.filePropertiesProfileFilePropertyValue)(f => where((f.profile_id === profile_id)) select(f) orderBy(f.file_property_id asc)).toList
    
  }
}