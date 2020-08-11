package util

import model.{Account, AccountMember}
import model.SquerylEntryPoint._

/**
 * Created by sdoshi on 8/9/2014.
 */
trait ValidatedAccountActions {


  def UserIsAdmin(username: String) : ValidatedAction = {
    def execute() : Boolean = {
      inTransaction {
        val accountMember = AccountMember.get_username(username).head
        accountMember.is_admin
      }
    }
    new ValidatedAction(execute, "User is not an administrator of this account")
  }

  def UserSharesAccount(username: String, targetUsername: String) : ValidatedAction = {

    def execute() : Boolean = {
      inTransaction {
        val accountMember = AccountMember.get_username(username).head
        val targetUsername = AccountMember.get_username(username).head

        accountMember.account_id == targetUsername.account_id
      }
    }
    new ValidatedAction(execute, "User is not a member of the account")
  }

  def UserIsNotArchived(username :String) : ValidatedAction = {
    def execute() : Boolean = {
      inTransaction {
        val accountMember = AccountMember.get_username(username).head
        accountMember.archive_time.isEmpty
      }
    }
    new ValidatedAction(execute, "User is already archived")
  }

  def UserIsArchived(username :String) : ValidatedAction = {
    def execute() : Boolean = {
      inTransaction {
        val accountMember = AccountMember.get_username(username).head
        accountMember.archive_time.isDefined
      }
    }
    new ValidatedAction(execute, "User is already activate")
  }

  def UserIsNotLastAdmin(username: String) : ValidatedAction = {
    def execute() : Boolean = {
      inTransaction {
        val accountMember = AccountMember.get_username(username).head

        val accountAdmins = AccountMember.countAccountAdmins(accountMember.account_id)

        println(accountAdmins)
        println(accountMember.is_admin)

        accountMember.is_admin == false || AccountMember.countAccountAdmins(accountMember.account_id) > 1
      }
    }
    new ValidatedAction(execute, "Unable to perform this action on the last administrator of the account")
  }

  def UsernameIsAvailable(username: String) : ValidatedAction = {
    def execute() : Boolean = {
      inTransaction {
        AccountMember.get_username(username).isEmpty
      }
    }
    new ValidatedAction(execute, "Username is already taken")
  }


}
