package model

/**
 * Created with IntelliJ IDEA.
 * User: Sachin Doshi
 * Date: 7/6/13
 * Time: 8:43 PM
 * To change this template use File | Settings | File Templates.
 */
object TransferStatus extends Enumeration {
  type TransferStatus = Value

  val Initiated = Value(1, "Initiated")
  val Transferring = Value(2, "Transferring")
  val Complete = Value(3, "Complete")
  val Deleted = Value(4, "Deleted")
  val Deleting = Value(5, "Deleting")
  val Cancelled = Value(6, "Cancelled")
}
