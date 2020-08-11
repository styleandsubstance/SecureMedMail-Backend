import play.api.db.DB
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.Props
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.PostgreSqlAdapter
import util.FileCleanupActor
import util.DailyUploadDeletionByDateTask
import scala.concurrent.duration._
import util.FileDeletionTask

object Global extends GlobalSettings {
	override def onStart(app:Application):Unit =
	{
			Logger.info("Creating session factory")	
			SessionFactory.concreteFactory = Some(() => Session.create(DB.getConnection()(app),
					dbAdapter));

			Logger.info("Done creating session factory")
			//set up scheduled Akka tasks

			//scheduled task that runs daily to delete files from Amazon S3
			//val fileCleanupActor = Akka.system.actorOf(Props[FileCleanupActor])

			Logger.info("Done setting up akka")

			Akka.system.scheduler.schedule(0 seconds, 1 day, Akka.system.actorOf(Props[FileCleanupActor]), DailyUploadDeletionByDateTask)
			Akka.system.scheduler.schedule(0 seconds, 15 minutes, Akka.system.actorOf(Props[FileCleanupActor]), FileDeletionTask)
	}
	
	override def onStop(app:Application):Unit =
	{
	  //SessionFactory.concreteFactory.
	}
	
	val dbAdapter = new PostgreSqlAdapter();

}
