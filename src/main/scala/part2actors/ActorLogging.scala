package part2actors

import akka.actor.Actor
import akka.event.Logging

object ActorLogging extends App {

  class SimpleActorWithExplicitLogger extends Actor {
    val logger = Logging(context.system, this) // 1 - DEBUG, 2 - INFO, 3 - WARNING, 4 - ERROR

    override def receive: Receive = {
      case message => logger.info(message.toString) // LOG it
    }
  }

}
