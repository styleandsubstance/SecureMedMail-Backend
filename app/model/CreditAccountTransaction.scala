package model

import org.squeryl.KeyedEntity
import model.SquerylEntryPoint._
import org.squeryl.annotations.{Column}

class CreditAccountTransaction(
    var id: Long,
    var account_transaction_id: Long,
    var from_balance: BigDecimal,
    var to_balance: BigDecimal) extends KeyedEntity[Long] {

    lazy val accounTransaction: AccountTransaction = {
      Library.accountTransactionToCreditAccountTransaction.right(this).head
    }
}

object CreditAccountTransaction {
  
  def getMostRecentCreditTransaction(account_id: Long) : Option[CreditAccountTransaction] = {
    from(Library.creditAccountTransaction, Library.accountTransaction)((cat, at) =>
      where(cat.account_transaction_id === at.id and at.account_id === account_id)
      select(cat)
      orderBy(at.transaction_time desc)
    ).headOption
  }
}