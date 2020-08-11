package controllers.webservices

import java.sql.Timestamp

import play.api.libs.json._

case class DojoRange(firstResult: Int, lastResult: Int, numResults: Int)
case class DojoSort(field: Option[String], descending: Boolean)

trait DojoStoreParser {
  
  def parseRange(range: String): DojoRange = {
    val IndexOfDash: Int = range.indexOf("-");
    val IndexOfEqual = range.indexOf("=");

    val firstResult = range.substring(IndexOfEqual + 1, IndexOfDash).toInt;
    val endResult = range.substring(IndexOfDash + 1, range.length()).toInt;
    
    val numResults = (endResult - firstResult) + 1;
    
    DojoRange(firstResult, endResult, numResults);
  }
  
  def parseSort(dojoSortString: String): DojoSort = {
    val sortPattern = """sort\(([_+-])(.+)\)""".r
    sortPattern.findFirstIn(dojoSortString).map( sortString => {
      val rawSortString = sortString.drop(6).dropRight(1)
      val descending: Boolean = sortString.drop(5).startsWith("-")
      new DojoSort(Option(rawSortString), descending)
    }).getOrElse(new DojoSort(None, true))
  }

  def parseEndDate(numMilliseconds: Long) : Timestamp = {
    return new Timestamp(86399999 + numMilliseconds)
  }
}