package part4faulttolerance

import java.io.File

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{Backoff, BackoffSupervisor}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.io.Source

object BackoffSupervisorPattern extends App {

  case object ReadFile
  class FileBasedPersistentActor extends Actor with ActorLogging {
    var dataSource: Source = null

    override def preStart(): Unit = {
      log.info(s"Persistent Actor Starting.")
    }

    override def postStop(): Unit = {
      log.warning(s"Persistent Actor has stopped.")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.warning(s"Persistent Actor restarting.")
    }

    override def receive: Receive = {
      case ReadFile =>
        if (dataSource == null)
          dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_data.txt"))
        log.info(s"I've just read some important data: ${dataSource.getLines().toList}")
    }
  }

  val system = ActorSystem("BackoffSupervisorDemo")
  // val simpleActor = system.actorOf(Props[FileBasedPersistentActor], "simpleActor")
  // simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
    Backoff.onFailure(
      childProps = Props[FileBasedPersistentActor],
      childName = "simpleBackoffActor",
      minBackoff = 3 seconds,
      maxBackoff = 30 seconds,
      randomFactor = 0.2
    )
  )

  // val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
  // simpleBackoffSupervisor ! ReadFile

  /*
  simpleSupervisor
  - child called simpleBackoffActor (props of type FileBasedPersistentActor)
  - supervision strategy is the default one (restarting on everything)
    - first attempt after 3 seconds
    - next attempt is 2x the previous attempt
   */

  val stopSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      childProps = Props[FileBasedPersistentActor],
      childName = "stopBackoffActor",
      minBackoff = 3 seconds,
      maxBackoff = 30 seconds,
      randomFactor = 0.2
    ).withSupervisorStrategy(
      OneForOneStrategy() {
        case _ => Stop
      }
    )
  )

//  val stopBackoffSupervisor = system.actorOf(stopSupervisorProps, "simpleStopSupervisor")
//  stopBackoffSupervisor ! ReadFile

  class EagerFileBasedPersistentActor extends FileBasedPersistentActor {
    override def preStart(): Unit = {
      log.info(s"Eager actor starting")
      dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_data.txt"))
      log.info(s"I've just read some important data: ${dataSource.getLines().toList}")
    }
  }

  // val eagerActor = system.actorOf(Props[EagerFileBasedPersistentActor], "eagerActor")
  // Default ActorInitializationException strategy => STOP

  val repeatedSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      childProps = Props[EagerFileBasedPersistentActor],
      childName = "eagerActor",
      minBackoff = 1 second,
      maxBackoff = 30 seconds,
      randomFactor = 0.1
    )
  )
  val repeatedSupervisor = system.actorOf(repeatedSupervisorProps, "eagerSupervisor")
  /*
  eagerSupervisor
  - child eagerActor
   - will die on start with ActorInitializationException
   - trigger the supervision strategy in eagerSupervisor => STOP eagerActor
  - backoff will kick in after 1 second, 2s, 4,s, 8s, 16s.
   */

}
