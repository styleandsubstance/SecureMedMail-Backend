package controllers.webservices

import model.Plan
import play.api.libs.json._
import play.api.libs.json.{JsValue, Writes, Reads, Json}


/**
 * Created by Sachin Doshi on 8/1/2014.
 */

case class AjaxResponse[T](success: Boolean, data: Option[List[T]], error: Option[String], redirectUrl: Option[String])

object AjaxResponse {



  implicit val plansWrites = new Writes[Plan] {
    def writes(value: Plan) : JsValue = {
      Json.obj(
        "id" -> value.id,
        "name" -> value.name,
        "description" -> value.description,
        "default" -> value.is_default,
        "download_rate" -> value.download_rate,
        "upload_rate" -> value.upload_rate,
        "charge" -> value.charge
      )
    }
  }

  def ajaxResponseToJson[T](value: AjaxResponse[T], dataValue: JsValue) = {
    Json.obj(
      "success" -> value.success,
      "data" -> dataValue,
      "error" -> value.error,
      "redirectUrl" -> value.redirectUrl
    )
  }

  implicit val ajaxResponseWrites = new Writes[AjaxResponse[String]] {
    def writes(value: AjaxResponse[String]): JsValue = {
      ajaxResponseToJson(value, Json.toJson(value.data))
    }
  }

  implicit val ajaxResponseWritesForPlan: Writes[AjaxResponse[Plan]] = new Writes[AjaxResponse[Plan]] {
    def writes(value: AjaxResponse[Plan]): JsValue = {
      ajaxResponseToJson(value, Json.toJson(value.data.map(a => a.toList)))
    }
  }

}


