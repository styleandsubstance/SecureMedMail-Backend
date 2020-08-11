package model

import java.sql.Timestamp
import java.util.Date

import model.AccountTransactionClass.AccountTransactionClass
import org.squeryl.annotations.{Column}
import org.squeryl._
import model.SquerylEntryPoint._
import org.squeryl.dsl.fsm.Conditioned
import org.squeryl.dsl.fsm.WhereState
import org.squeryl.dsl.{GroupWithMeasures}
import org.squeryl.internals.StatementWriter
import org.squeryl.dsl.ast.{OrderByExpression, ExpressionNode}
import model.dao.{AccountTransactionSortParam, AccountTransactionFindCriterion, AccountTransactionSearch}
import model.dao.AccountTransactionSortParam._


class AccountTransaction(
    var id: Long,
    var account_id: Long,
    var transaction_time: Timestamp,
    var account_transaction_type_id: Long,
    var account_transaction_class_id: Long,
    var amount: BigDecimal
  ) extends KeyedEntity[Long] {
  
  
  lazy val account: Account = {
    Library.accountToAccountTransaction.right(this).head
  }
  
}

object AccountTransaction {



  def buildWhereClause(account_id: Long, searchCriteria: AccountTransactionSearch,
                       accountTransaction: AccountTransaction,
                       upload: Option[Upload], uploadForDownload: Option[Upload], accountMemberForUpload: Option[AccountMember]) : WhereState[Conditioned] = {
    val startTimestamp = searchCriteria.startDate.getOrElse(new Timestamp(0))
    val endTimestamp = searchCriteria.endDate.getOrElse(new Timestamp(new Date().getTime()))

    val globalSearch: Option[String] = searchCriteria.globalSearch.map(searchString => Option("%" + searchString.toLowerCase() + "%")).getOrElse(None)

    val whereClause = where(
      (accountTransaction.account_id === account_id ) and
        (accountTransaction.transaction_time gte startTimestamp).inhibitWhen(searchCriteria.startDate == None) and
        (accountTransaction.transaction_time lte endTimestamp).inhibitWhen(searchCriteria.endDate == None) and
        (accountTransaction.account_transaction_type_id === searchCriteria.transactionType).inhibitWhen(searchCriteria.transactionType == None) and
        (accountTransaction.account_transaction_class_id === searchCriteria.transactionClass).inhibitWhen(searchCriteria.transactionClass == None) and
        (accountMemberForUpload.map(_.username) === searchCriteria.username).inhibitWhen(searchCriteria.username == None) and
        ((lower(upload.map(u => u.filename)) like globalSearch) or
          (lower(uploadForDownload.map(u => u.filename)) like globalSearch) or
          (lower(upload.map(u => u.guid)) like globalSearch) or
          (lower(uploadForDownload.map(u => u.guid)) like globalSearch) or
          (lower(accountMemberForUpload.map(am => am.username)) like globalSearch)).inhibitWhen(searchCriteria.globalSearch == None)
    )

    whereClause
  }

//  def buildSort(sort: AccountTransactionSortParam, descending: Boolean,
//                accountTransaction: AccountTransaction, uat: Option[UploadAccountTransaction], dat: Option[DownloadAccountTransaction],
//                cat: Option[CreditAccountTransaction], bat: Option[BillingAccountTransaction], u: Option[Upload],
//                d: Option[Download], u2: Option[Upload]): ExpressionNode = {
//    val orderByArg: OrderByExpression = {
//      if ( sort ==  AccountTransactionSortParam.transaction_time) {
//        new OrderByExpression(accountTransaction.transaction_time);
//      }
//      else if ( sort == AccountTransactionSortParam.charge_amount) {
//        new OrderByExpression(accountTransaction.amount);
//      }
//      else if ( sort == AccountTransactionSortParam.credit_amount) {
//        new OrderByExpression(cat.);
//      }
//      else if ( sort == UploadSortParam.confirmed_download_count) {
//        new OrderByExpression(upload.confirmed_download_count);
//      }
//      else if ( sort == UploadSortParam.filename) {
//        new OrderByExpression(upload.filename);
//      }
//      else {
//        new OrderByExpression(upload.description);
//      }
//    }
//
//    if ( descending == true ) {
//      orderByArg.inverse
//    }
//    else {
//      orderByArg
//    }
//  }

//  def findAccountTransactionsForAccountId(account_id: Long, search: AccountTransactionSearch, criterion: AccountTransactionFindCriterion) = {
//    join(Library.accountTransaction, Library.uploadAccountTransaction.leftOuter, Library.downloadAccountTransaction.leftOuter,
//         Library.creditAccountTransaction.leftOuter, Library.billingAccountTransaction.leftOuter,
//         Library.upload.leftOuter, Library.download.leftOuter, Library.upload.leftOuter)((accountTransaction, uat, dat, cat, bat, u, d, u2) =>
//      buildWhereClause(account_id, search, accountTransaction, u, u2)
//      select(accountTransaction, uat, dat, cat, bat, u, d, u2)
//      orderBy(accountTransaction.transaction_time desc)
//      on(accountTransaction.id === uat.map(_.account_transaction_id), accountTransaction.id === dat.map(_.account_transaction_id),
//         accountTransaction.id === cat.map(_.account_transaction_id), accountTransaction.id === bat.map(_.account_transaction_id),
//         uat.map(_.upload_id) === u.map(_.id), dat.map(_.download_id) === d.map(_.id), d.map(_.upload_id) === u2.map(_.id))
//    ).page(criterion.offset, criterion.pageLength).toList
//  }

  def findAccountTransactionsForAccountId(account_id: Long, search: AccountTransactionSearch, criterion: AccountTransactionFindCriterion) = {
    join(Library.accountTransaction, Library.uploadAccountTransaction.leftOuter, Library.downloadAccountTransaction.leftOuter,
      Library.creditAccountTransaction.leftOuter, Library.billingAccountTransaction.leftOuter,
      Library.upload.leftOuter, Library.download.leftOuter, Library.upload.leftOuter, Library.accountMember.leftOuter)(
        (accountTransaction, uat, dat, cat, bat, u, d, u2, uam) =>
          buildWhereClause(account_id, search, accountTransaction, u, u2, uam)
          select(accountTransaction, uat, dat, cat, bat, u, d, u2, uam)
          orderBy(accountTransaction.transaction_time desc)
          on(accountTransaction.id === uat.map(_.account_transaction_id), accountTransaction.id === dat.map(_.account_transaction_id),
            accountTransaction.id === cat.map(_.account_transaction_id), accountTransaction.id === bat.map(_.account_transaction_id),
            uat.map(_.upload_id) === u.map(_.id), dat.map(_.download_id) === d.map(_.id), d.map(_.upload_id) === u2.map(_.id),
            u.map(_.uploaded_by_member_id) === uam.map(_.id))
      ).page(criterion.offset, criterion.pageLength).toList
  }

  def countAccountTransactionsForAccountId(account_id: Long, search: AccountTransactionSearch) = {
    join(Library.accountTransaction, Library.uploadAccountTransaction.leftOuter, Library.downloadAccountTransaction.leftOuter,
      Library.creditAccountTransaction.leftOuter, Library.billingAccountTransaction.leftOuter,
      Library.upload.leftOuter, Library.download.leftOuter, Library.upload.leftOuter, Library.accountMember.leftOuter)(
        (accountTransaction, uat, dat, cat, bat, u, d, u2, uam) =>
          buildWhereClause(account_id, search, accountTransaction, u, u2, uam)
          compute(countDistinct(accountTransaction.id))
          on(accountTransaction.id === uat.map(_.account_transaction_id), accountTransaction.id === dat.map(_.account_transaction_id),
            accountTransaction.id === cat.map(_.account_transaction_id), accountTransaction.id === bat.map(_.account_transaction_id),
            uat.map(_.upload_id) === u.map(_.id), dat.map(_.download_id) === d.map(_.id), d.map(_.upload_id) === u2.map(_.id),
            u.map(_.uploaded_by_member_id) === uam.map(_.id))
    )
  }

  def getTotal(account_id: Long, search: AccountTransactionSearch, transactionClass: AccountTransactionClass): BigDecimal = {

    val searchForChargeTotal: AccountTransactionSearch = new AccountTransactionSearch(search.startDate, search.endDate, search.globalSearch,
          search.transactionType, Option(transactionClass.id), search.username)

    println(search.transactionType)
    println(searchForChargeTotal.transactionType)

    join(Library.accountTransaction, Library.uploadAccountTransaction.leftOuter, Library.downloadAccountTransaction.leftOuter,
      Library.creditAccountTransaction.leftOuter, Library.billingAccountTransaction.leftOuter,
      Library.upload.leftOuter, Library.download.leftOuter, Library.upload.leftOuter, Library.accountMember.leftOuter)(
        (accountTransaction, uat, dat, cat, bat, u, d, u2, uam) =>
        buildWhereClause(account_id, searchForChargeTotal, accountTransaction, u, u2, uam)
        compute(sum(accountTransaction.amount))
        on(accountTransaction.id === uat.map(_.account_transaction_id), accountTransaction.id === dat.map(_.account_transaction_id),
          accountTransaction.id === cat.map(_.account_transaction_id), accountTransaction.id === bat.map(_.account_transaction_id),
          uat.map(_.upload_id) === u.map(_.id), dat.map(_.download_id) === d.map(_.id), d.map(_.upload_id) === u2.map(_.id),
          u.map(_.uploaded_by_member_id) === uam.map(_.id))
      ).head.measures.getOrElse(BigDecimal(0))
  }
}