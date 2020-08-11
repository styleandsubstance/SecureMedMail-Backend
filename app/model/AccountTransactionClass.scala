package model

object AccountTransactionClass extends Enumeration {
	type AccountTransactionClass = Value
	
	val Billing = Value(1, "Billing")
	val Credit = Value(2, "Credit")
	val Pending = Value(3, "Pending")
}