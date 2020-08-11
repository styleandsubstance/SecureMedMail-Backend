package util

import model.AccountMember
import play.api.libs.iteratee.{Input, Done}
import play.api.libs.json.JsValue
import play.api.mvc._
import controllers.routes
import services.StreamingError
import services.StreamingSuccess
import model.SquerylEntryPoint._
import play.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Sachin Doshi
 * Date: 7/7/13
 * Time: 8:21 PM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("username")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.AccountManagement.login)

  //private def onArchived(request: RequestHeader) = Results.BadRequest("This user has been archived")
  //private def onArchived(request: RequestHeader) = Results.Redirect(Results.BadRequest("This user has been archived"))
  private def onArchived(request: RequestHeader) = Results.BadRequest("Testing")

  private def onUploadsDisallowed(request: RequestHeader) = Results.BadRequest("This user is not permitted to upload files")

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }
  
  def IsAuthenticated(bp: BodyParser[MultipartFormData[Either[StreamingError, StreamingSuccess]]])(f: => String => Request[MultipartFormData[Either[StreamingError, StreamingSuccess]]] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(bp) { request => f(user)(request) }
  }

  /*
  def IsAuthenticatedAndActiveAndAuthorized(bp: BodyParser[MultipartFormData[Either[StreamingError, StreamingSuccess]]])(f: => String => Request[MultipartFormData[Either[StreamingError, StreamingSuccess]]] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(bp) { request =>

      val accountMember: Option[AccountMember] = transaction {
        AccountMember.get_username(user).headOption
      }

      if ( accountMember.isEmpty ) {
        Results.BadRequest("This user has been archived")
      }
      else if ( accountMember.get.uploads_allowed == false ) {
        Results.BadRequest("Uploads have been disallowed for this user")
      }
      else {
        f(user)(request)
      }
    }
  }
  */


  def IsAuthenticatedJson(bp: BodyParser[JsValue])(f: => String => Request[JsValue] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(bp) { request => f(user)(request) }
  }

  /**
   * Check if the connected user is a member of this project.
   */
  /*
  def IsMemberOf(project: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    if(Project.isMember(project, user)) {
      f(user)(request)
    } else {
      Results.Forbidden
    }
  }

  /**
   * Check if the connected user is a owner of this task.
   */
  def IsOwnerOf(task: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    if(Task.isOwner(task, user)) {
      f(user)(request)
    } else {
      Results.Forbidden
    }
  }
  */

}

