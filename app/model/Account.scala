package model

import java.sql.Timestamp
import java.util.Date
import org.squeryl._
import model.SquerylEntryPoint._
import org.squeryl.annotations.{Column}
import org.squeryl.dsl.ManyToOne
import org.squeryl.dsl.OneToMany
import scala.math.BigDecimal
import play.Logger

class Account(
    var id: Long,
    var organization: String,
    var address_line_1: String,
    var address_line_2: Option[String],
    var city: String,
    var state: String,
    var zipcode: String,
    var cc_name: String,
    var cc_address_line_1: String,
    var cc_address_line_2: Option[String],
    var cc_city: String,
    var cc_state: String,
    var cc_zipcode: String,
    var cc_number: String,
    var cc_exp_month: String,
    var cc_exp_year: String,
    var cc_security_code: String,
    var plan_id: Long) extends KeyedEntity[Long] {


   //var admin: AccountMember = {
   //  Library.adminToAccount.right(this).head
   //}

  lazy val accountMembers: List[AccountMember] = {
    Library.accountToAccountMembers.left(this).toList
  }
  
  lazy val plan: Plan = {
    Library.planToAccount.right(this).head
  }
  
}


object Account {

  def apply(organization: String, address_line_1: String,
      address_line_2: Option[String], city: String, state: String, zipcode: String,
      creditCard: CreditCard, plan_id: Int): Account = {
    var account = new Account(0, organization,
        address_line_1, address_line_2, city, state, zipcode, 
        creditCard.cc_name, creditCard.cc_address_line_1, creditCard.cc_address_line_2,
        creditCard.cc_city, creditCard.cc_state, creditCard.cc_zipcode, creditCard.cc_number, creditCard.cc_exp_month, creditCard.cc_exp_year, creditCard.cc_security_code, plan_id)
    account
  }




  def unapply(account: Account): Option[(String, String, Option[String], String, String, String, CreditCard, Int)] = {
     Option(account.organization, account.address_line_1, account.address_line_2,
         account.city, account.state, account.zipcode,
         new CreditCard(account.cc_name, account.cc_address_line_1, account.cc_address_line_2, account.cc_city, account.cc_state, account.cc_zipcode,
             account.cc_number, account.cc_exp_month, account.cc_exp_year, account.cc_security_code),
         account.plan_id.toInt)
  }
  
  
  
  /*
   * Database Function
   */
  def getAcccountById(account_id: Long): Account = {
    from(Library.account)(a => where(a.id === account_id) select(a)).single
  }
  
  def getAccountByIdForUpdate(account_id: Long): Account = {
    from(Library.account)(a => where(a.id === account_id) select(a)).forUpdate.single
  }
  
  def getCurrentCreditBalance(account_id: Long) : BigDecimal = {
    val mostRecentCreditTransaction: Option[CreditAccountTransaction] = 
      CreditAccountTransaction.getMostRecentCreditTransaction(account_id);
    println(mostRecentCreditTransaction)
    mostRecentCreditTransaction.map(a => a.to_balance).getOrElse(BigDecimal(0))
  }
  
  
  def listAccountMembersForAccount(account_id: Long): List[AccountMember] = {
    from(Library.accountMember)(am => where(am.account_id === account_id) select(am) orderBy(am.username)).toList
  }
}