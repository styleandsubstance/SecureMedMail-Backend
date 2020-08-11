package model

import org.squeryl.KeyedEntity
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import play.api.libs.json.Json

class UploadFileProperty(
    var id: Long,
    var upload_id: Long,
    var file_property_id: Long,
    var file_property_value: Option[String]) 
    	extends KeyedEntity[Long] {

    
    lazy val file_property: FileProperty = {
      Library.filePropertyToUploadFileProperty.right(this).headOption.get
    }
    
    def isTrue: Boolean = {
      if ( file_property_value.isDefined && file_property_value.get.toLowerCase() == "true" )
        true
      else
        false
    }
    
    def toLong: Long = {
      try {
        file_property_value.getOrElse("0").toLong
      } catch {
      	case _ : java.lang.NumberFormatException => 0
      }
    } 
}


object UploadFileProperty {
  implicit val uploadPropertyWrite = new Writes[UploadFileProperty] {
    def writes(value: UploadFileProperty): JsValue = {
      Json.obj(
        "name" -> value.file_property.name,
        "type" -> FilePropertyType(value.file_property.type_id.toInt).toString(),
        "value" -> value.file_property_value
      )
    }
  }
}