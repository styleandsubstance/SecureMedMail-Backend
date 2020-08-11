package model.dao

/**
 * Created by Sachin Doshi on 7/4/2014.
 */
object AccountTransactionSortParam extends Enumeration {
  type AccountTransactionSortParam = Value

  val transaction_time = Value("transaction_time")
  val charge_amount = Value("charge_amount")
  val credit_amount = Value("credit_amount")
  val pending_amount = Value("pending_amount")
  val charge_type = Value("charge_type")
  val charge_class = Value("charge_class")
  val guid = Value("guid")
  val filename = Value("filename")


}
