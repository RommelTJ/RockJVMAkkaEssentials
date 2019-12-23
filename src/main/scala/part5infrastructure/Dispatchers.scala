package part5infrastructure

import akka.actor.{Actor, ActorLogging}

object Dispatchers extends App {

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] ${message.toString}")
    }
  }

}
