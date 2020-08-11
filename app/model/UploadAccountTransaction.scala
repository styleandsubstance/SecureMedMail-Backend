package model

import org.squeryl.KeyedEntity

class UploadAccountTransaction(
    var id: Long,
    var account_transaction_id: Long,
    var upload_id: Long) extends KeyedEntity[Long] {
  
   lazy val accounTransaction: AccountTransaction = {
     Library.accountTransactionToUploadAccountTransaction.right(this).headOption.get
   }
  
   lazy val upload: Upload = {
     Library.uploadToUploadAccountTransaction.right(this).headOption.get
   }
}