package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}

object ActorLogging extends App {

  // Method #1: Explicit Logging
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

  // Method #2: Actor Logging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = ???
  }

}
