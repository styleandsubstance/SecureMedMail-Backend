package controllers.webservices

import java.sql.Timestamp
import java.util.Date

import akka.actor.Props
import model.SquerylEntryPoint._
import _root_.util._
import play.Logger
import play.api.Play.current
import play.api.data.validation.ValidationError
import play.api.libs.concurrent.Akka
import play.api.libs.json._
import play.api.libs.json.{JsValue, Writes, Reads, Json}
import play.api.libs.functional.syntax._
import play.api.mvc.{Action, Controller}
import model._


case class AccountOrganzationNode(name: String, childeren: List[AccountMember])
case class UpdateAccountMember(username: String, first_name: String, last_name: String, email: String);
case class NewAccountMember(username: String, first_name: String, last_name: String, email: String, password: String);
case class ArchiveAccountMember(username: String, reason: Option[String]);
case class UsernameAvailability(username: String, reason: Option[String])
case class UpdateAccountMemberPassword(username: String, password: String)
case class OrganizationProfile(organization: String, address_line_1: String, address_line_2: Option[String],
                               city: String, state: String, zipcode: String)
case class OrganizationCreditCard(cc_name: String, cc_address_line_1: String,
                      cc_address_line_2: Option[String], cc_city: String, cc_state: String, cc_zipcode: String,
                      cc_number: String, cc_exp_month: String, cc_exp_year: String, cc_security_code: String)
case class OrganizationPlan(plan_id: Int, reason: Option[String]);
case class NewAccount(organization: OrganizationProfile, creditCard: OrganizationCreditCard,
                      plan: OrganizationPlan, admin: NewAccountMember);


object AccountWebServicesController extends Controller with Secured with ValidatedAccountActions with ValidatedDatabaseActions {

  implicit val updateAccountMemberReads: Reads[UpdateAccountMember] = (
      (__ \ "username").read[String] and
      (__ \ "first_name").read[String] and
      (__ \ "last_name").read[String] and
      (__ \ "email").read[String] (Reads.email)
    )(UpdateAccountMember)


  implicit val newAccountMemberReads: Reads[NewAccountMember] = (
    (__ \ "username").read[String](
      Reads.filter[String](ValidationError("Username contains invalid characters"))(AccountMember.verify_username_constraints(_))
        keepAnd Reads.filter[String](ValidationError("Username is already taken"))(AccountMember.verify_username_availability(_))  ) and
    (__ \ "first_name").read[String] and
    (__ \ "last_name").read[String] and
    (__ \ "email").read[String] (Reads.email) and
    (__ \ "password").read[String] (Reads.filter[String](ValidationError("Password doesn't meet complexity requirements"))(AccountMember.verify_password_constraints(_)))
  )(NewAccountMember)


  implicit val archiveAccountMemberReads: Reads[ArchiveAccountMember] = (
    (__ \ "username").read[String] and
    (__ \ "reason").readNullable[String]
  )(ArchiveAccountMember)


  implicit val updateAccountMemberPasswordReads: Reads[UpdateAccountMemberPassword] = (
    (__ \ "username").read[String] and
    (__ \ "password").read[String](Reads.filter[String](ValidationError("Password contains invalid characters"))(AccountMember.verify_password_constraints(_)))
  )(UpdateAccountMemberPassword)

  implicit val usernameAvailabilityReads: Reads[UsernameAvailability] = (
    (__ \ "username").read[String] and
    (__ \ "reason").readNullable[String]
  )(UsernameAvailability)

  implicit val organizationProfileReads: Reads[OrganizationProfile] = (
    (__ \ "organization").read[String] and
      (__ \ "address_line_1").read[String] and
      (__ \ "address_line_2").readNullable[String] and
      (__ \ "city").read[String] and
      (__ \ "state").read[String] and
      (__ \ "zipcode").read[String]
  )(OrganizationProfile)

  implicit val organizationCreditCardReads: Reads[OrganizationCreditCard] = (
    (__ \ "cc_name").read[String] and
    (__ \ "cc_address_line_1").read[String] and
    (__ \ "cc_address_line_2").readNullable[String] and
    (__ \ "cc_city").read[String] and
    (__ \ "cc_state").read[String] and
    (__ \ "cc_zipcode").read[String] and
    (__ \ "cc_number").read[String] and
    (__ \ "cc_exp_month").read[String] and
    (__ \ "cc_exp_year").read[String] and
    (__ \ "cc_security_code").read[String]
  )(OrganizationCreditCard)

  implicit val organizationPlanReads: Reads[OrganizationPlan] = (
    (__ \ "plan_id").read[Int] and
    (__ \ "reason").readNullable[String]
  )(OrganizationPlan)


  implicit val newAccountReads: Reads[NewAccount] = (
    (__ \ "organization").read[OrganizationProfile] and
    (__ \ "creditcard").read[OrganizationCreditCard] and
    (__ \ "plan").read[OrganizationPlan] and
    (__ \ "admin").read[NewAccountMember]
  )(NewAccount)


  implicit val accountMemberWrites = new Writes[AccountMember] {
    def writes(value: AccountMember): JsValue = {
      Json.obj(
        "id" -> value.username,
        "name" -> value.username,
        "username" -> value.username,
        "first_name" -> value.first_name,
        "last_name" -> value.last_name,
        "email" -> value.email,
        "archived_time" -> value.archive_time,
        "creation_time" -> value.creation_time
      )
    }
  }
  
  implicit val accountOrganizationNodeWrites = new Writes[AccountOrganzationNode] {
    def writes(value: AccountOrganzationNode): JsValue = {
      Json.obj(
        "name" -> value.name,
        "children" -> Json.toJson(value.childeren)
      )
    }
  }

  def listAccountOrganzationAccountMembers = IsAuthenticated { username => implicit request =>
    
    val accountOrganzationNode = transaction {
      val accountMember = AccountMember.get_username(username).head
      val account = Account.getAcccountById(accountMember.account_id)
      val accountMembers = Account.listAccountMembersForAccount(account.id)
      
      new AccountOrganzationNode(account.organization, accountMembers)
      
    }
    
    Ok(Json.toJson(accountOrganzationNode))
  }

  def updateAccountMember =  IsAuthenticatedJson(parse.json) { username => implicit request =>
    request.body.validate[UpdateAccountMember].fold(
      valid = { res => {

        def database_function() = {
          var accountMember = AccountMember.get_username(res.username).head
          accountMember.first_name = res.first_name;
          accountMember.last_name = res.last_name;
          accountMember.email = res.email;
          val savedAccountMember = Library.accountMember.update(accountMember);
        }

        val result = execute({
          List(UserIsAdmin(username),
            UserSharesAccount(username, res.username),
            UserIsNotArchived(res.username))
        },
        database_function
        )

        Ok(Json.toJson(AjaxResponse[String](result.success, None, result.errorMessage, None)))
      }},
      invalid = {e => Ok(Json.toJson(AjaxResponse[String](false, None, Option(JsError.toFlatJson(e).toString), None)))}
    )
  }

  def saveNewOrganizationAccountMember = IsAuthenticatedJson(parse.json) { username => implicit request =>
    request.body.validate[NewAccountMember].fold(
        valid = { res => {
          transaction {
            val accountMember = AccountMember.get_username(username).head

            val newAccountMember = new AccountMember(
              id = 0,
              username = res.username,
              password = AccountMember.encrypt_password(res.password),
              email = res.email,
              account_id = accountMember.account_id,
              archive_time = None,
              creation_time = new Timestamp(new Date().getTime),
              first_name = res.first_name,
              last_name = res.last_name,
              created_by_account_member_id = Option(accountMember.id),
              is_admin = false,
              downloads_allowed = true,
              uploads_allowed = true
            );

            val savedAccountMember = Library.accountMember.insert(newAccountMember);
            val defaultProfile = Library.filePropertiesProfile.insert(
              new FilePropertiesProfile(0, "Default", Option[String]("Default File Properties"),
                savedAccountMember.id, true))
            FilePropertiesProfile.assoicateDefaultFileProperties(defaultProfile)
          }

          Ok(Json.toJson(AjaxResponse[String](true, None, None, None)))
        }},
        invalid = {e => Ok(Json.toJson(AjaxResponse[String](false, None, Option(JsError.toFlatJson(e).toString), None)))}
      )
  }


  def archiveAccountMember = IsAuthenticatedJson(parse.json) { username => implicit request =>
    request.body.validate[ArchiveAccountMember].fold(
      valid = { res => {

        def database_function() = {
          var accountMember = AccountMember.get_username(res.username).head
          accountMember.archive_time = Option(new Timestamp(new Date().getTime()))
          val savedAccountMember = Library.accountMember.update(accountMember);
        }

        val result = execute({
           List(UserIsAdmin(username),
            UserSharesAccount(username, res.username),
            UserIsNotArchived(res.username),
            UserIsNotLastAdmin(res.username))
          },
          database_function
        )

        Ok(Json.toJson(AjaxResponse[String](result.success, None, result.errorMessage, None)))
      }},
      invalid = {e => Ok(Json.toJson(AjaxResponse[String](false, None, Option(JsError.toFlatJson(e).toString), None)))}
    )
  }

  def activateAccountMember = IsAuthenticatedJson(parse.json) { username => implicit request =>
    request.body.validate[ArchiveAccountMember].fold(
      valid = { res => {
        def database_function() = {
          var accountMember = AccountMember.get_username(res.username).head
          accountMember.archive_time = None
          val savedAccountMember = Library.accountMember.update(accountMember);
        }

        val result = execute({
          List(UserIsAdmin(username),
            UserSharesAccount(username, res.username),
            UserIsArchived(res.username))
        },
        database_function
        )

        Ok(Json.toJson(AjaxResponse[String](result.success, None, result.errorMessage, None)))
      }},
      invalid = {e => Ok(Json.toJson(AjaxResponse[String](false, None, Option(JsError.toFlatJson(e).toString), None)))}
    )
  }

  def verifyUsernameAvailibility = Action(parse.json) { implicit request =>
    request.body.validate[UsernameAvailability].fold(
      valid = { res => {
        def database_function() = {
        }

        val result = execute({
          List(UsernameIsAvailable(res.username))
        },
          database_function
        )

        Ok(Json.toJson(AjaxResponse[String](result.success, None, None, None)))
      }},
      invalid = {e => Ok(Json.toJson(AjaxResponse[String](false, None, Option(JsError.toFlatJson(e).toString), None)))}
    )
  }

  def listAvailablePlans = Action { implicit request =>

    val plans: List[Plan] = transaction {
      Plan.listAllActivePlans.toList
    }

    Ok(Json.toJson(AjaxResponse[Plan](true, Option(plans), None, None)))

  }

  def updateAccountMemberPassword = IsAuthenticatedJson(parse.json) { username => implicit request =>
    request.body.validate[UpdateAccountMemberPassword].fold(
      valid = { res => {
        def database_function() = {
          var accountMember = AccountMember.get_username(res.username).head
          accountMember.password = AccountMember.encrypt_password(res.password)
          val savedAccountMember = Library.accountMember.update(accountMember);
        }

        val result = execute({
          List(UserIsAdmin(username),
            UserSharesAccount(username, res.username),
            UserIsNotArchived(res.username))
        },
        database_function
        )

        Ok(Json.toJson(AjaxResponse[String](result.success, None, result.errorMessage, None)))
      }},
      invalid = {e => Ok(Json.toJson(AjaxResponse[String](false, None, Option(JsError.toFlatJson(e).toString), None)))}
    )
  }

  def createNewAccountOld = Action(parse.json) { implicit request =>
    request.body.validate[NewAccount].fold(
      valid = { res => {
        def database_function() = {

            val validatedAccount = new Account(0, res.organization.organization, res.organization.address_line_1,
              res.organization.address_line_2, res.organization.city, res.organization.state, res.organization.zipcode,
              res.creditCard.cc_name, res.creditCard.cc_address_line_1, res.creditCard.cc_address_line_2,
              res.creditCard.cc_city, res.creditCard.cc_state, res.creditCard.cc_zipcode, res.creditCard.cc_number,
              res.creditCard.cc_exp_month, res.creditCard.cc_exp_year, res.creditCard.cc_security_code,
              res.plan.plan_id)

            var theNewAccount = Library.account.insert(validatedAccount)

            Logger.debug("[AccountManagement.createNewAccount] - Inserted new account with account_id: " + theNewAccount.id);

            var validatedAccountMemberFormSubmission = res.admin

            var validatedAccountMember = AccountMember(validatedAccountMemberFormSubmission.username,
              validatedAccountMemberFormSubmission.password, validatedAccountMemberFormSubmission.email, None, new Timestamp(new Date().getTime),
              validatedAccountMemberFormSubmission.first_name, validatedAccountMemberFormSubmission.last_name)

            validatedAccountMember.account_id = theNewAccount.id;
            validatedAccountMember.password = AccountMember.encrypt_password(validatedAccountMember.password)
            validatedAccountMember = Library.accountMember.insert(validatedAccountMember);
            var defaultProfile = Library.filePropertiesProfile.insert(new FilePropertiesProfile(0, "Default", Option[String]("Default File Properties"), validatedAccountMember.id, true))
            FilePropertiesProfile.assoicateDefaultFileProperties(defaultProfile)

            Library.accountPlanTransition.insert(new AccountPlanTransition(
              0, theNewAccount.id, validatedAccountMember.id, None, theNewAccount.plan_id, new Timestamp(new Date().getTime())))

            validatedAccountMember
        }

        val result = execute({
          List(UsernameIsAvailable(res.admin.username))
        },
          database_function
        )


        //Bill the account for the plan as necessary
        Akka.system.actorOf(Props[BillingActor]) ! BillPlan(result.data.get.account_id)

        Thread.sleep(20000);
        Ok(Json.toJson(AjaxResponse[String](result.success, None, None, Option(controllers.routes.AccountManagement.index.toString))))
          .withSession("username" -> result.data.get.username)
      }},
      invalid = {e => Ok(Json.toJson(AjaxResponse[String](false, None, Option(JsError.toFlatJson(e).toString), None)))}
    )
  }

  def createNewAccount = Action(parse.json) { implicit request =>
    Thread.sleep(5000);
    Ok(Json.toJson(AjaxResponse[String](true, None, None, Option(controllers.routes.AccountManagement.index.toString))))
      .withSession("username" -> "jsmith")

  }
}





