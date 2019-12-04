package part2actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}

object ActorLogging extends App {

  class SimpleActorWithExplicitLogger extends Actor {
    val logger: LoggingAdapter = Logging(context.system, this) // 1 - DEBUG, 2 - INFO, 3 - WARNING, 4 - ERROR

    override def receive: Receive = {
      case message => logger.info(message.toString) // LOG it
    }
  }

  // Testing
  val system = ActorSystem("LoggingDemo")
  val simpleActorWithExplicitLogger = system.actorOf(Props[SimpleActorWithExplicitLogger])
  simpleActorWithExplicitLogger ! "Logging a simple message"

}
