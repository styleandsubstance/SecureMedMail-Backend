package controllers.webservices

import java.sql.Timestamp

import controllers.webservices.UploadedFileStore._
import model.dao._
import model._
import model.SquerylEntryPoint._
import play.api.libs.json.{JsValue, Writes, Json}
import play.api.mvc.{AnyContent, Request, Controller}
import util.Secured

/**
 * Created by Sachin Doshi on 7/4/2014.
 */



case class AccountTransactionsTotalResponse(charge_total: String, credit_total: String, pending_total: String)



object AccountTransactionStore extends Controller with Secured with DojoStoreParser {

  def get_filename(accountTransaction: AccountTransaction, upload: Option[Upload], uploadForDownload: Option[Upload]) : String = {
    if ( accountTransaction.account_transaction_type_id == AccountTransactionType.Upload.id) {
      upload.map(_.filename).getOrElse("")
    }
    else if (accountTransaction.account_transaction_type_id == AccountTransactionType.Download.id) {
      uploadForDownload.map(_.filename).getOrElse("")
    }
    else {
      ""
    }
  }

  def get_billing_amount(accountTransaction: AccountTransaction) : String = {
    if ( accountTransaction.account_transaction_class_id == AccountTransactionClass.Billing.id) {
      accountTransaction.amount.toString()
    }
    else {
      ""
    }
  }

  def get_credit_amount(accountTransaction: AccountTransaction) : String = {
    if ( accountTransaction.account_transaction_class_id == AccountTransactionClass.Credit.id) {
      accountTransaction.amount.toString()
    }
    else {
      ""
    }
  }

  def get_pending_amount(accountTransaction: AccountTransaction) : String = {
    if ( accountTransaction.account_transaction_class_id == AccountTransactionClass.Pending.id) {
      accountTransaction.amount.toString()
    }
    else {
      ""
    }
  }

  implicit val accountTransactionWrite: Writes[(model.AccountTransaction, Option[model.UploadAccountTransaction], Option[model.DownloadAccountTransaction], Option[model.CreditAccountTransaction], Option[model.BillingAccountTransaction], Option[model.Upload], Option[model.Download], Option[model.Upload], Option[model.AccountMember])] =
      new Writes[(model.AccountTransaction, Option[model.UploadAccountTransaction], Option[model.DownloadAccountTransaction], Option[model.CreditAccountTransaction], Option[model.BillingAccountTransaction], Option[Upload], Option[model.Download], Option[model.Upload], Option[model.AccountMember])] {
    def writes(value: (model.AccountTransaction, Option[model.UploadAccountTransaction], Option[model.DownloadAccountTransaction], Option[model.CreditAccountTransaction], Option[model.BillingAccountTransaction], Option[Upload], Option[model.Download], Option[model.Upload], Option[AccountMember])): JsValue = {
      //Json.toJson(value).
      Json.obj(
        "transaction_time" -> value._1.transaction_time,
        "billing_amount" -> get_billing_amount(value._1),
        "credit_amount" -> get_credit_amount(value._1),
        "pending_amount" -> get_pending_amount(value._1),
        "charge_type" -> AccountTransactionType(value._1.account_transaction_type_id.toInt).toString,
        "charge_class" -> AccountTransactionClass(value._1.account_transaction_class_id.toInt).toString,
        "guid" -> value._6.map(_.guid).getOrElse(value._8.map(_.guid).getOrElse("")).toString,
        "filename" -> get_filename(value._1, value._6, value._8),
        "from_balance" -> value._4.map(_.from_balance.toString),
        "to_balance" -> value._4.map(_.to_balance.toString),
        "upload_username" -> value._9.map(_.username).getOrElse("").toString)
    }
  }

  implicit val accountTransactionTotalsResponseWrite: Writes[AccountTransactionsTotalResponse] =
    new Writes[AccountTransactionsTotalResponse] {
      def writes(value: AccountTransactionsTotalResponse) : JsValue = {
        Json.obj(
          "charge_total" -> ("$" + value.charge_total.toString),
          "credit_total" -> ("$" + value.credit_total.toString),
          "pending_total" -> ("$" + value.pending_total.toString)
        )
      }
    }


  def buildFindCriteria(dojoRangeOptions: DojoRange, dojoSortOptions: DojoSort): AccountTransactionFindCriterion = {
    val sortString = dojoSortOptions.field.getOrElse("transaction_time")
    val sortParam = AccountTransactionSortParam.values.map(_.toString()).find(_ == sortString).map(AccountTransactionSortParam.withName(_)).getOrElse(AccountTransactionSortParam.transaction_time)
    new AccountTransactionFindCriterion(dojoRangeOptions.firstResult, dojoRangeOptions.numResults, sortParam, dojoSortOptions.descending)
  }

  def buildSearchCriteria(request: Request[AnyContent]) = {
    val startDate: Option[Timestamp] = request.getQueryString("startDate").map(s => new Timestamp(s.toLong))
    val endDate: Option[Timestamp] = request.getQueryString("endDate").map(s => parseEndDate(s.toLong))
    val globalSearch: Option[String] = request.getQueryString("globalSearch")
    val transactionType = request.getQueryString("transactionType").getOrElse("")
    val transactionTypeId: Option[Long] = AccountTransactionType.values.map(_.toString()).find(_ == transactionType).map(AccountTransactionType.withName(_).id)
    val transactionClass = request.getQueryString("transactionClass").getOrElse("")
    val transactionClassId: Option[Long] = AccountTransactionClass.values.map(_.toString()).find(_ == transactionClass).map(AccountTransactionClass.withName(_).id)
    val username: Option[String] = request.getQueryString("username")

    new AccountTransactionSearch(startDate, endDate, globalSearch, transactionTypeId, transactionClassId, username)
  }



  def getAccountTransactions = IsAuthenticated { username => implicit request =>
    val range = parseRange(request.headers.get("Range").getOrElse("items=0-24"))
    val sort = parseSort(request.rawQueryString)
    val findCriteria = buildFindCriteria(range, sort)
    val searchCriteria = buildSearchCriteria(request)

    val searchResults = transaction {
      val accountMember = AccountMember.get_username(username).head
      val totalAccountTransactions = AccountTransaction.countAccountTransactionsForAccountId(accountMember.account_id, searchCriteria)
      val accountTransactions = AccountTransaction.findAccountTransactionsForAccountId(accountMember.account_id, searchCriteria , findCriteria)

      println(totalAccountTransactions.head.measures.toString)

      (totalAccountTransactions.head.measures.toString, accountTransactions)
    }

    val contentRange = "items " + range.firstResult + "-" + range.lastResult + "/" + searchResults._1;

    Ok(Json.toJson(searchResults._2)).withHeaders(CONTENT_RANGE -> contentRange)

  }


  def getAccountTransactionsTotal = IsAuthenticated { username => implicit request =>
    val searchCriteria = buildSearchCriteria(request)

    val totals = transaction {

      val accountMember = AccountMember.get_username(username).head

      val chargeTotal: BigDecimal = {
        if ( searchCriteria.transactionClass.isDefined && searchCriteria.transactionClass.get != AccountTransactionClass.Billing.id) {
          BigDecimal(0)
        }
        else {
          AccountTransaction.getTotal(accountMember.account_id, searchCriteria, AccountTransactionClass.Billing)
        }
      }

      val creditTotal: BigDecimal = {
        if ( searchCriteria.transactionClass.isDefined && searchCriteria.transactionClass.get != AccountTransactionClass.Credit.id) {
          BigDecimal(0)
        }
        else {
          AccountTransaction.getTotal(accountMember.account_id, searchCriteria, AccountTransactionClass.Credit)
        }
      }

      val pendingTotal: BigDecimal = {
        if ( searchCriteria.transactionClass.isDefined && searchCriteria.transactionClass.get != AccountTransactionClass.Pending.id) {
          BigDecimal(0)
        }
        else {
          AccountTransaction.getTotal(accountMember.account_id, searchCriteria, AccountTransactionClass.Pending)
        }
      }

      new AccountTransactionsTotalResponse(chargeTotal.toString, creditTotal.toString, pendingTotal.toString)
    }



    Ok(Json.toJson(totals))
  }
}
