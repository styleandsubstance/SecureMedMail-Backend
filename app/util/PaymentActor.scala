package util

import akka.actor.Actor
import model.AccountTransaction
import play.Logger
import model.SquerylEntryPoint._
import model.AccountTransactionClass
import model.Library
import model.BillingAccountTransaction


case class ChargeAccountForTransaction(accountTransaction: AccountTransaction)

class PaymentActor extends Actor {

  def receive = {
    case ChargeAccountForTransaction(accountTransaction) => {
      
      transaction {
        
        accountTransaction.account_transaction_class_id = AccountTransactionClass.Billing.id;
        Library.accountTransaction.update(accountTransaction)
        
        Library.billingAccountTransaction.insert(new BillingAccountTransaction(
            0, accountTransaction.id, "SUCCESS"))
      }
    }
    case _ => {
    	Logger.error("Unknown message type received in PaymentActor")
    }
    
  }
  
  
}