package util

import akka.actor.Actor
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import play.Play;
import play.Logger;

case class EmailNotification(emailAddress: String, subject: String, message: String);

class NotificationActor  extends Actor {
	def receive = {
	  case EmailNotification(emailAddress, subject, message) => {
	     Logger.debug("Sending e-mail to: " + emailAddress + " with subject: " + subject + " and body: " + message);
	     try {
	    	 var email: Email = new SimpleEmail();
		     email.setHostName(Play.application().configuration().getString("notifications.smtp.address"));
		     email.setSmtpPort(Play.application().configuration().getString("notifications.smtp.port").toInt);
		     email.setAuthenticator(new DefaultAuthenticator(
		         Play.application().configuration().getString("notifications.username"), 
		         Play.application().configuration().getString("notifications.password")));
		     email.setSSLOnConnect(true);
		     email.setFrom(Play.application().configuration().getString("notifications.username"));
		     email.setSubject(subject);
		     email.setMsg(message);
		     email.addTo(emailAddress);
		     email.send();
	     } catch {
	       case e: Exception => {
	         Logger.error("Error while sending e-mail download notification", e);
	       }
	     }
	  }
	}
}