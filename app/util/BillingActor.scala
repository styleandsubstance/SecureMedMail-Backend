package util

import akka.actor._
import play.Logger
import model.Download
import model.Upload
import model.SquerylEntryPoint._
import model.Account
import model.FilePropertyEnum
import play.api.Play.current
import play.api.libs.concurrent.Akka
import model.AccountTransaction
import model.DownloadAccountTransaction
import model.Library
import java.sql.Timestamp
import java.util.Date
import model.AccountTransactionType
import model.AccountTransactionClass
import model.UploadAccountTransaction
import model.Plan
import model.PlanAccountTransaction
import akka.pattern.Patterns
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import model.CreditAccountTransaction
import akka.util.Timeout

case class BillDownload(download: Download)
case class BillUpload(upload: Upload)
case class BillPlan(account_id: Long)
case class BillAccount(accountTransaction: AccountTransaction)
 

class BillingActor extends Actor {
  
    def billAccountForPlan(account_id: Long) = {
      val accountTransaction : Option[AccountTransaction] = inTransaction {
	      val account: Account = Account.getAccountByIdForUpdate(account_id)
	      val plan = account.plan;
	      
	      if ( plan.charge > 0) {
	        
  	        val accountTransaction = Library.accountTransaction.insert(new AccountTransaction(
	            0, account.id, new Timestamp(new Date().getTime()), AccountTransactionType.Plan.id, AccountTransactionClass.Pending.id, plan.charge));
  	        
	        
  	        val planAccountTransaction = Library.planAccountTransaction.insert(new PlanAccountTransaction(
	            0, accountTransaction.id, plan.id));
	        
  	        val currentAccountBalance:BigDecimal =  Account.getCurrentCreditBalance(account.id)
  	        val toAccountBalance = currentAccountBalance + plan.charge
  	        
  	        //create a credit transaction to increase the account's balance
  	        val creditAccountTransaction = Library.creditAccountTransaction.insert(new CreditAccountTransaction(
  	            0, accountTransaction.id, currentAccountBalance, toAccountBalance))
  	        
  	        Option(accountTransaction)
	      }
	      else {
	        None
	      }
	    }
	    
	    accountTransaction.map(accountTransaction => {
	      Akka.system.actorOf(Props[PaymentActor]) ! ChargeAccountForTransaction(accountTransaction)
	    })
	    .getOrElse({
	      Logger.debug("No plan transaction....not billing account")
	    })
	    
	    println("Done billing plan")
    }
  
	def receive = {
	  case BillDownload(download) => {
	    
	    val accountIdToBill: Option[Long] = transaction {
	    
  	      //determine who should be billed for this download
	      val billDownloadToUploaderFileProperty = 
		    download.upload.upload_file_properties.find(
		        uploadFileProperty => uploadFileProperty.file_property_id == FilePropertyEnum.BillDownloadToUploader.id);
	    
	      val billDownloadToUploader: Boolean = 
	        billDownloadToUploaderFileProperty.map(f=> f.isTrue).getOrElse(false);
	    
	      if (billDownloadToUploader) {
	        Some(download.upload.uploaded_by_member.account_id)
	      }
	      else {
	        //download.downloaded_by_member.map(account => Some(account.account_id)).getOrElse(None)
	        Download.getDownloadedByAccountMember(download.downloaded_by_member_id).map(am => Some(am.account_id)).getOrElse(None)
	        
	      }
	    }  
	    
	    accountIdToBill.map(account_id => {
	        val accountTransaction = transaction {
		        val account = Account.getAccountByIdForUpdate(account_id)
		      
		        val accountTransaction = Library.accountTransaction.insert(new AccountTransaction(
		            0, account_id, new Timestamp(new Date().getTime()), AccountTransactionType.Download.id, AccountTransactionClass.Pending.id, account.plan.download_rate));
		      
		        val downloadAccountTransaction = Library.downloadAccountTransaction.insert(new DownloadAccountTransaction(
		            0, accountTransaction.id, download.id));
		        
		        accountTransaction
	        }
	      
	        Akka.system.actorOf(Props[BillingActor]) ! BillAccount(accountTransaction)
	      }
	    ).getOrElse(
	       Logger.error("Error while trying to bill download: " + download.id + ".  Unable to determine which account to bill")
	    )
	  }
	  case BillUpload(upload: Upload) => {
	    val accountTransaction = transaction {
	        val account = Account.getAccountByIdForUpdate(upload.uploaded_by_member.account_id)
	      
	        val accountTransaction = Library.accountTransaction.insert(new AccountTransaction(
	            0, account.id, new Timestamp(new Date().getTime()), AccountTransactionType.Upload.id, AccountTransactionClass.Pending.id, account.plan.download_rate));
	      
	        val uploadAccountTransaction = Library.uploadAccountTransaction.insert(new UploadAccountTransaction(
	            0, accountTransaction.id, upload.id));
	        
	        accountTransaction
        }
      
        Akka.system.actorOf(Props[BillingActor]) ! BillAccount(accountTransaction)
	    

	  }
	  case BillPlan(account_id: Long) => {
	      transaction {
	        billAccountForPlan(account_id)
	      }
	  }
	  case BillAccount(accountTransaction: AccountTransaction) => {
	    
	    //get the current account balance to see if we should potentially
	    //bill their plan
	    transaction {
	      val account: Account = Account.getAccountByIdForUpdate(accountTransaction.account_id)
	      val currentBalance = Account.getCurrentCreditBalance(account.id)
	      
	      println("Account current balance: " + currentBalance)
	      
          //check the current account balance against amount of this transaction  
	      if ( currentBalance <= accountTransaction.amount) {
	    	  billAccountForPlan(accountTransaction.account_id)
	      }
	    }
	    
   	    val accountTransactionToCharge: Option[AccountTransaction] = transaction {
          val account: Account = Account.getAccountByIdForUpdate(accountTransaction.account_id)
	      val newBalance: BigDecimal = Account.getCurrentCreditBalance(account.id)
	      
	      println("Current account balance: " + newBalance)
	      
	      println("Account transaction amount: " + accountTransaction.amount)
	      
	      if ( newBalance >= accountTransaction.amount) {
	        
	        println("Using credits")
	        
	        val balanceAfterTransaction = newBalance - accountTransaction.amount
	        
	        println("Using credits")
	        
	        //in this case, all we need is to update the credit balance
	        val creditAccountTransaction = Library.creditAccountTransaction.insert(new CreditAccountTransaction(
	            0, accountTransaction.id, newBalance, balanceAfterTransaction))
	            
	        //now update the account transaction from pending to credit    
	        accountTransaction.account_transaction_class_id = AccountTransactionClass.Credit.id
	        accountTransaction.transaction_time = new Timestamp(new Date().getTime())
	        Library.accountTransaction.update(accountTransaction)
	        
	        None
	      }
	      else {
	        //notify Credit Card Actor to charge the account for
	        //this transaction
	        Option(accountTransaction)
	      }
	    }
   	    
   	    accountTransactionToCharge.map(at => {
   	      println("Charging credit card")
	      Akka.system.actorOf(Props[PaymentActor]) ! ChargeAccountForTransaction(accountTransaction)
   	    })
	  }
      case _ => {
        Logger.error("Unknown message type received in BillingActor")
      }
   }

}