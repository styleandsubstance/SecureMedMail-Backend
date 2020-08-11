package model.dao

import model.dao.AccountTransactionSortParam.AccountTransactionSortParam
import model.dao.UploadSortParam._
import java.util.Date
import java.sql.Timestamp

trait Pageable {
  def offset: Int
  def pageLength: Int
}

trait Sortable {
  def descending: Boolean
}

//case class FindCriteria(offset: Int, pageLength: Int, sort: Value, sortDescending: Boolean);

case class UploadSearch(startDate: Option[Timestamp], endDate: Option[Timestamp], globalSearch: Option[String],
                        transferStatusId: Option[Long], username: Option[String])
case class UploadFindCriterion(offset: Int, pageLength: Int, sort: UploadSortParam, descending: Boolean)
case class AccountTransactionSearch(startDate: Option[Timestamp], endDate: Option[Timestamp], globalSearch: Option[String],
                                    transactionType: Option[Long], transactionClass: Option[Long], username: Option[String])
case class AccountTransactionFindCriterion(offset: Int, pageLength: Int, sort: AccountTransactionSortParam, descending: Boolean)