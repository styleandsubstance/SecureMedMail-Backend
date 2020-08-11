

package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import model.{Library, AccountMember, Account, CreditCard}
import org.squeryl._
import model.SquerylEntryPoint._
import util.Secured
import model.FilePropertiesProfile
import model.FileProperty
import play.Logger
import views.ReCaptcha
import model.Plan
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import util.BillingActor
import util.BillPlan
import model.AccountPlanTransition
import java.sql.Timestamp
import java.util.Date


case class AccountMemberFormSubmission(username: String, password: String, password_confirm: String, email: String, first_name: String, last_name: String)


/**
 * Created with IntelliJ IDEA.
 * User: Sachin Doshi
 * Date: 7/6/13
 * Time: 6:54 PM
 * To change this template use File | Settings | File Templates.
 */
object AccountManagement  extends Controller with Secured {

  val loginForm = Form(
    tuple(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )verifying("Invalid username/password",result => result match {

        case (u, p) => {
          transaction {
            AccountMember.authenticate(u,p)
          }
        }
    })
  )

  val accountCreateForm = Form(
    tuple(
      "account" ->mapping(
          "organization" -> nonEmptyText,
          "address_line_1" -> nonEmptyText,
          "address_line_2" -> optional(text),
          "city" -> nonEmptyText,
          "state" -> nonEmptyText,
          "zipcode" -> nonEmptyText,
          "creditcard" -> mapping(
            "cc_name" -> nonEmptyText,
            "cc_address_line_1" -> nonEmptyText,
            "cc_address_line_2" -> optional(text),
            "cc_city" -> nonEmptyText,
            "cc_state" -> nonEmptyText,
            "cc_zipcode" -> nonEmptyText(5, 5),
            "cc_number" -> nonEmptyText(16, 16),
            "cc_exp_month" -> nonEmptyText,
            "cc_exp_year" -> nonEmptyText,
            "cc_security_code" -> nonEmptyText(3, 4)
          )(CreditCard.apply)(CreditCard.unapply),
          "plan_id" -> number.verifying("Invalid plan selection", plan_id => {
        	  transaction {
        	    Logger.debug("Plan ID: " + plan_id)
        	    Plan.getPlanById(plan_id).isEmpty == false
        	  }
          })
      )(Account.apply)(Account.unapply),
      "userinfo" -> mapping(
        "username" -> nonEmptyText(5, 32)
        	.verifying("Username is taken",result => result match {
        		case(u) => transaction {
        			AccountMember.get_username(u).isEmpty
        		}
        	}),
        "password" -> nonEmptyText(5, 32)
        	.verifying("Password must contain at least one capital letter, number, and a special character", 
        	    p => AccountMember.verify_password_constraints(p)
        	),
        "password_confirm" -> nonEmptyText(5, 32),
        "email" -> email,
        "first_name" -> nonEmptyText,
        "last_name" -> nonEmptyText
      )(AccountMemberFormSubmission.apply)(AccountMemberFormSubmission.unapply)
      .verifying("Passwords do not match", result => result match {
        case(am) => {
          am.password == am.password_confirm
        }
      }),
      "recaptcha_challenge_field" -> nonEmptyText,
      "recaptcha_response_field" -> nonEmptyText
    )
  )

  def index = IsAuthenticated { username => implicit request =>
      Ok(views.html.accounthome())
  }


  def login = Action {

    println(routes.AccountManagement.login)


    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      errors => {
        Logger.debug("Error during authentication")
        BadRequest(views.html.login(errors));
      },
      authenticated => {
        Redirect(routes.AccountManagement.index).withSession("username" -> authenticated._1)
      }
    )
  }

  def newAccount = Action {
    Ok(views.html.accountcreate(accountCreateForm))
  }

  def createNewAccount = Action { implicit request =>
    accountCreateForm.bindFromRequest.fold(
      errors => {
        Logger.debug("[AccountManagement.createNewAccount] - New Account form submission failed! " + errors);
        BadRequest(views.html.accountcreate(errors));
      },
      validatedForm => {
        
        
        if ( ReCaptcha.check(request.remoteAddress, validatedForm._3, validatedForm._4) == false ) {
          Logger.info("Bad ReCaptcha request from ip: " + request.remoteAddress)
          BadRequest(views.html.accountcreate(accountCreateForm.fill(validatedForm).withGlobalError("ReCapthca verification failed")))
        }
        else {
            Logger.debug("[AccountManagement.createNewAccount] - All Data successfully validated.  Inserting into database.");
            
	        val newAccountMember = transaction {
	          val validatedAccount = validatedForm._1;
	
	          var theNewAccount = Library.account.insert(validatedAccount)
	
	          Logger.debug("[AccountManagement.createNewAccount] - Inserted new account with account_id: " + theNewAccount.id);
	
	          var validatedAccountMemberFormSubmission = validatedForm._2;
	          
	          var validatedAccountMember = AccountMember(validatedAccountMemberFormSubmission.username, 
	              validatedAccountMemberFormSubmission.password, validatedAccountMemberFormSubmission.email, None, new Timestamp(new Date().getTime),
                validatedAccountMemberFormSubmission.first_name, validatedAccountMemberFormSubmission.last_name)
	          
	          validatedAccountMember.account_id = theNewAccount.id;
	          validatedAccountMember.password = AccountMember.encrypt_password(validatedAccountMember.password)
	          validatedAccountMember = Library.accountMember.insert(validatedAccountMember);
	          var defaultProfile = Library.filePropertiesProfile.insert(new FilePropertiesProfile(0, "Default", Option[String]("Default File Properties"), validatedAccountMember.id, true))
	          FilePropertiesProfile.assoicateDefaultFileProperties(defaultProfile)
	          
	          //Library.adminToAccount.left(validatedAccountMember).associate(theNewAccount)
	          
	          Library.accountPlanTransition.insert(new AccountPlanTransition(
	              0, theNewAccount.id, validatedAccountMember.id, None, theNewAccount.plan_id, new Timestamp(new Date().getTime())))
	          
	          validatedAccountMember
	        }
	        
	        //Bill the account for the plan as necessary
	        Akka.system.actorOf(Props[BillingActor]) ! BillPlan(newAccountMember.account_id) 
	
	        Redirect(routes.AccountManagement.index).withSession("username" -> newAccountMember.username)
        }
      }
    )
  }
  
  def uploadFileManagement = IsAuthenticated { username => implicit request =>
    Ok(views.html.UploadedFilesManagement())
  }

  def billingManagement = IsAuthenticated { username => implicit request =>
    Ok(views.html.BillingManagement())
  }


  def logout = Action { implicit request =>
   Ok(views.html.index("Welcome!")).withNewSession
  }
  
  def accountMemberManagement = IsAuthenticated { username => implicit request =>
   Ok(views.html.AccountMemberManagement()) 
  }
  
  
  /*
   * Useful functions
   */
  
  def getAllPlans() : List[Plan] = {
    transaction {
      Plan.listAllActivePlans.toList
    }
  }
}




