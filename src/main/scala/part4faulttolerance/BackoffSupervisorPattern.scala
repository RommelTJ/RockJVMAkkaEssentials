package part4faulttolerance

import akka.actor.{Actor, ActorLogging}

object BackoffSupervisorPattern extends App {

  class FileBasedPersistentActor extends Actor with ActorLogging {
    case object ReadFile
    override def receive: Receive = ???
  }

}
