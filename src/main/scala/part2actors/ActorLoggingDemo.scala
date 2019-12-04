package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}

object ActorLoggingDemo extends App {

  // Method #1: Explicit Logging
  class SimpleActorWithExplicitLogger extends Actor {
    val logger: LoggingAdapter = Logging(context.system, this) // 1 - DEBUG, 2 - INFO, 3 - WARNING, 4 - ERROR

    override def receive: Receive = {
      case message => logger.info(message.toString) // LOG it
    }
  }

  // Testing Method 1
  val system = ActorSystem("LoggingDemo")
  val simpleActorWithExplicitLogger = system.actorOf(Props[SimpleActorWithExplicitLogger])
  simpleActorWithExplicitLogger ! "Logging a simple message"

  // Method #2: Actor Logging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b) => log.info("Two things: {} and {}", a, b) // interpolating strings in logs
      case message => log.info(message.toString)
    }
  }

  // Testing Method 2
  val actorWithLogging = system.actorOf(Props[ActorWithLogging])
  actorWithLogging ! "Logging another simple message"
  actorWithLogging ! ("One", 65)

}
