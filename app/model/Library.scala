package model

import org.squeryl._
import model.SquerylEntryPoint._
import java.lang.Long

object Library extends Schema {

  //Table Account
  val account = table[Account]("account")
  on(account)(a => declare(
    a.id is (autoIncremented("account_id_seq") )
  ))
  
  //Table AccountMember
  val accountMember = table[AccountMember]("accountmember")
  on(accountMember)(a => declare(
    a.username is (unique),
    a.id is(autoIncremented("accountmember_id_seq"))
  ))
  
  val upload = table[Upload]("upload")
  on(upload)(u => declare(
    u.guid is (unique),
    u.id is (autoIncremented("upload_id_seq") )
  ))
  
  val download = table[Download]("download")
  on(download)(d => declare(
    d.guid is (unique),
    d.id is (autoIncremented("download_id_seq") )
  ))
  
  val plan = table[Plan]("plan")
  on(plan)(p => declare(
    p.id is (autoIncremented("plan_id_seq") )
  ))
  
  //Upload File Properties
  val fileProperty = table[FileProperty]("fileproperty")
  on(fileProperty)(fp => declare(
    fp.id is (autoIncremented("fileproperty_id_seq") )
  ))  
  
  val filePropertiesProfile = table[FilePropertiesProfile]("filepropertiesprofile")
  on(filePropertiesProfile)(fpp => declare(
    fpp.id is (autoIncremented("filepropertiesprofile_id_seq")),
    columns(fpp.name, fpp.account_member_id) are(unique))
  )  
  
  val filePropertiesProfileFilePropertyValue = table[FilePropertiesProfileFilePropertyValue]("filepropertiesprofilefilepropertyvalue")
    on(filePropertiesProfileFilePropertyValue)(fppfpv => declare(
    fppfpv.id is (autoIncremented("filepropertiesprofilefilepropertyvalue_id_seq") )
  ))
  
  val uploadFilePropertyValue = table[UploadFileProperty]("uploadfilepropertyvalue")
  on(uploadFilePropertyValue)(ufp => declare(
    ufp.id is (autoIncremented("uploadfilepropertyvalue_id_seq"))
  ))
  
  //Billing Tables
  val accountPlanTransition = table[AccountPlanTransition]("accountplantransition")
  on(accountPlanTransition)(t => declare(
    t.id is (autoIncremented("accountplantransition_id_seq"))
  ))
  
  /*
   * Account Trasaction
   */
  val accountTransaction = table[AccountTransaction]("accounttransaction")
  on(accountTransaction)(t => declare(
    t.id is (autoIncremented("accounttransaction_id_seq"))
  ))
  

  /*
   * Billing Account Transaction
   */
  val billingAccountTransaction = table[BillingAccountTransaction]("billingaccounttransaction")
  on(billingAccountTransaction)(t => declare(
    t.id is (autoIncremented("billingaccounttransaction_id_seq"))
  ))
  
  /*
   * Credit Account Transaction
   */   
  val creditAccountTransaction = table[CreditAccountTransaction]("creditaccounttransaction")
  on(creditAccountTransaction)(t => declare(
    t.id is (autoIncremented("creditaccounttransaction_id_seq"))
  ))
  
  /*
   * Plan Account Transaction
   */
  val planAccountTransaction = table[PlanAccountTransaction]("planaccounttransaction")
  on(planAccountTransaction)(t => declare(
    t.id is (autoIncremented("planaccounttransaction_id_seq"))
  ))
  
  /*
   * Download Account Transaction
   */
  val downloadAccountTransaction = table[DownloadAccountTransaction]("downloadaccounttransaction")
  on(downloadAccountTransaction)(t => declare(
    t.id is (autoIncremented("downloadaccounttransaction_id_seq"))
  ))

  /*
   *   Upload Account Transaction
   */   
  val uploadAccountTransaction = table[UploadAccountTransaction]("uploadaccounttransaction")
  on(uploadAccountTransaction)(t => declare(
    t.id is (autoIncremented("uploadaccounttransaction_id_seq"))
  ))
  
  val uploadToUploadAccountTransaction =    
   oneToManyRelation(upload, uploadAccountTransaction).
     via((u, uat) => u.id === uat.upload_id)
  
  
  val accountTransactionToUploadAccountTransaction =    
   oneToManyRelation(accountTransaction, uploadAccountTransaction).
     via((at, uat) => at.id === uat.account_transaction_id)  
  
  //Table relations
//  val adminToAccount =
//    oneToManyRelation(accountMember, account).
//      via((am, ai) => am.id === ai.admin_id)

  val accountToAccountMembers =
    oneToManyRelation(account, accountMember).
      via((ai, am) => ai.id === am.account_id)

  val accountMemberToAccountMemberCreatedBy =
    oneToManyRelation(accountMember, accountMember).
      via((am, createdBy) => am.id === createdBy.created_by_account_member_id)

  //val accountMemberToDownloads =
  //  oneToManyRelation(accountMember, download).
  //    via((am, d) => am.id === nvl(d.downloaded_by_member_id, 0))

  val accountMemberToUploads =
    oneToManyRelation(accountMember, upload).
      via((am, u) => am.id === u.uploaded_by_member_id)

  val planToAccount =
    oneToManyRelation(plan, account).
      via((p, a) => p.id === a.plan_id)

  val filePropertyToUploadFileProperty =
    oneToManyRelation(fileProperty, uploadFilePropertyValue).
      via((fp, ufp) => fp.id === ufp.file_property_id)
      
  val uploadToUploadFileProperties = 
    oneToManyRelation(upload, uploadFilePropertyValue).
      via((u, ufp) => u.id === ufp.upload_id)
  
  val profileToFilePropertyValues = 
    oneToManyRelation(filePropertiesProfile, filePropertiesProfileFilePropertyValue).
      via((fpp, fppv) => fpp.id === fppv.profile_id)
    
  val uploadToDownloads = 
  	oneToManyRelation(upload, download).
      via((u, d) => u.id === d.upload_id)
      
  val downloadToUploadAccountTransaction =    
   oneToManyRelation(download, downloadAccountTransaction).
     via((d, dat) => d.id === dat.download_id)
     
  val accountToAccountTransaction =
    oneToManyRelation(account, accountTransaction).
      via((a, at) => a.id === at.account_id)
      
  val accountTransactionToBillingAccountTransaction =
   oneToManyRelation(accountTransaction, billingAccountTransaction).
     via((at, bat) => at.id === bat.account_transaction_id)  

  val accountTransactionToCreditAccountTransaction =
   oneToManyRelation(accountTransaction, creditAccountTransaction).
     via((at, cat) => at.id === cat.account_transaction_id)

  val planToPlanAccountTransaction =    
   oneToManyRelation(plan, planAccountTransaction).
     via((p, pat) => p.id === pat.plan_id)
     
  val accountTransactionToPlanAccountTransaction =    
   oneToManyRelation(accountTransaction, planAccountTransaction).
     via((at, pat) => at.id === pat.account_transaction_id)      
  
  val accountTransactionToDownloadAccountTransaction =    
   oneToManyRelation(accountTransaction, downloadAccountTransaction).
     via((at, dat) => at.id === dat.account_transaction_id)
  
  

}