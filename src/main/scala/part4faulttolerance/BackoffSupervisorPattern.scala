package part4faulttolerance

import java.io.File

import akka.actor.{Actor, ActorLogging}

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
          dataSource = Source.fromFile(new File("src/main/resources/testfiles/important.txt"))
        log.info(s"I've just read some important data: ${dataSource.getLines().toList}")
    }
  }

}
