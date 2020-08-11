package model

import org.squeryl.KeyedEntity

class BillingAccountTransaction(
    var id: Long,
    var account_transaction_id: Long,
    var credit_card_api_response: String) extends KeyedEntity[Long] {

  
    lazy val accounTransaction: AccountTransaction = {
      Library.accountTransactionToBillingAccountTransaction.right(this).headOption.get
    }
  
}