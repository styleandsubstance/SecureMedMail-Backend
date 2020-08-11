package model

import java.sql.Timestamp
import java.util.Date
import org.squeryl._
import model.SquerylEntryPoint._
import org.squeryl.annotations.{Column}
import org.squeryl.dsl.ManyToOne
import org.squeryl.dsl.OneToMany
import scala.Some
import org.mindrot.jbcrypt.BCrypt


class AccountMember(
    var id: Long,
    var username: String,
    var password: String,
    var email: String,
    var account_id: Long,
    var archive_time: Option[Timestamp],
    var creation_time : Timestamp,
    var first_name: String,
    var last_name: String,
    var created_by_account_member_id: Option[Long],
    var is_admin: Boolean,
    var downloads_allowed: Boolean,
    var uploads_allowed: Boolean
    
    
    ) extends KeyedEntity[Long] {

  
  
}


object AccountMember {

  val MIN_USERNAME_LEN = 5;
  val MAX_USERNAME_LEN = 32;


  def apply(username: String, password: String, email: String,
            archive_time: Option[Timestamp], creation_time: Timestamp, first_name: String, last_name: String) : AccountMember = {
    new AccountMember(0, username, password, email, 0, archive_time, creation_time, first_name, last_name, None, true, true , true)
  }

  def unapply(accountMember: AccountMember) : Option[(String, String, String, Option[Timestamp], Timestamp, String, String)] = {
    Option(accountMember.username, accountMember.password, accountMember.email, accountMember.archive_time, accountMember.creation_time, accountMember.first_name, accountMember.last_name)
  }

  def encrypt_password(password: String) : String = {
    BCrypt.hashpw(password, BCrypt.gensalt(12));
  }

  def authenticate(username: String, password: String): Boolean =  {
    val user = get_username(username)

    if (user.isEmpty)
      false;
    else if ( user.head.archive_time.isDefined )
      false;
    else if (BCrypt.checkpw(password, get_password(username)) == false)
    	false;
    else
    	true;
  }

  def get_username(username: String): Iterable[AccountMember] = {
    from(Library.accountMember)(s => where(s.username === username) select(s))
  }
  
  def get_password(username: String) : String = {
    from(Library.accountMember)(s => where(s.username === username) select(s.password)).single
  }
  
  def findByAccountMemberId(account_member_id: Long): Iterable[AccountMember] = {
    from(Library.accountMember)(s => where(s.id === account_member_id) select(s))
  }

  def countAccountAdmins(account_id: Long) : Long = {
    from(Library.accountMember)(s => where(s.account_id === account_id and s.is_admin === true) compute(count(s.id)))
  }


  def verify_username_constraints(username: String) : Boolean = {
    val minLength = username.length >= AccountMember.MIN_USERNAME_LEN;
    val maxLength = username.length <= AccountMember.MAX_USERNAME_LEN;
    val isAscii = username.forall(c => c.isLetterOrDigit)
    val startsWithNonDigit = username.length > 0 && username.charAt(0).isLetter

    return ( minLength && maxLength && isAscii && startsWithNonDigit)
  }

  def verify_username_availability(username: String) : Boolean = {
    val accountMember: Option[AccountMember] = transaction {
      AccountMember.get_username(username).headOption
    }
    accountMember.isEmpty
  }

  def verify_password_constraints(password: String) : Boolean = {
    val specialCharacters: Array[Char] = "!@#$%^&*()<>?/|{}[]\\|+=-_".toCharArray();
    
    val containsUpperCase: Boolean = password.exists(c => c.isUpper)
    val containsNumber: Boolean = password.exists(c => c.isDigit)
    val containsSpecialCharacter: Boolean = password.exists(c => specialCharacters.contains(c))
    return ( containsUpperCase && containsNumber && containsSpecialCharacter)
  }
}