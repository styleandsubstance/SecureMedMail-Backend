package util

import model.SquerylEntryPoint._
import play.api.Logger

/**
 * Created by sdoshi on 8/9/2014.
 */

case class DatabaseActionResult[T](success: Boolean, errorMessage: Option[String], data: Option[T])

trait ValidatedDatabaseActions {

  def execute[T](validationFunctions: List[ValidatedAction], func: () => T) : DatabaseActionResult[T] = {
    val errorMessage : Option[String] = {
      transaction {
        validationFunctions.foldLeft[Option[String]](None)( (a, b) => {
         if ( a.isEmpty) {
           val success = b.execute();
           if ( !success ) {
             Option(b.errorMessage)
           }
           else {
             None
           }
         }
         else {
           a
         }
        })
      }
    }

    if ( errorMessage.isDefined ) {
      DatabaseActionResult[T](false, Option(errorMessage.get), None)
    }
    else {
      val databaseSuccess = {
        try {
          val databaseResult = transaction {
            func();
          }
          (None, Option(databaseResult))
        }
        catch {
          case e: RuntimeException => {
            Logger.warn("Error while executing database transaction: " + e.getMessage, e)
          }
          (Option("Error during database transaction"), None)
        }
      }

      databaseSuccess._1.map(e => DatabaseActionResult[T](false, Option(e), None)).getOrElse(DatabaseActionResult[T](true, None, databaseSuccess._2))
    }
  }


}
