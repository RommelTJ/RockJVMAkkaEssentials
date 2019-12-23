package part5infrastructure

import akka.actor.{Actor, ActorLogging}

object Routers extends App {

  class Master extends Actor {
    override def receive: Receive = ???
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

}
