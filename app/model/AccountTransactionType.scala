package model

object AccountTransactionType extends Enumeration {
	type AccountTransactionType = Value
	
	val Plan = Value(1, "Plan")
	val Upload = Value(2, "Upload")
	val Download = Value(3, "Download")
  
  
}