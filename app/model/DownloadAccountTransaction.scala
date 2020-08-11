package model

import org.squeryl.KeyedEntity

class DownloadAccountTransaction(
    var id: Long,
    var account_transaction_id: Long,
    var download_id: Long) extends KeyedEntity[Long] {

    lazy val accounTransaction: AccountTransaction = {
      Library.accountTransactionToDownloadAccountTransaction.right(this).headOption.get
    }
    
    lazy val downlaod: Download = {
      Library.downloadToUploadAccountTransaction.right(this).headOption.get
    }
}