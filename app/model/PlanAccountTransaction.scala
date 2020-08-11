package model

import org.squeryl.KeyedEntity

class PlanAccountTransaction(
    var id: Long,
    var account_transaction_id: Long,
    var plan_id: Long) extends KeyedEntity[Long] {

   lazy val accounTransaction: AccountTransaction = {
      Library.accountTransactionToPlanAccountTransaction.right(this).headOption.get
   }
   
   lazy val plan: Plan  = {
     Plan.getPlanById(plan_id).head
   }
}
