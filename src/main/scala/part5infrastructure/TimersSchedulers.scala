package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.concurrent.duration._
import scala.language.postfixOps

object TimersSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("SchedulersTimersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])
  system.log.info(s"Scheduling reminder for simpleActor")

  import system.dispatcher
  system.scheduler.scheduleOnce(delay = 1 second) {
    simpleActor ! "reminder"
  }

  val routine: Cancellable = system.scheduler.schedule(initialDelay = 1 second, interval = 2 seconds) {
    simpleActor ! "heartbeat"
  }

  system.scheduler.scheduleOnce(5 seconds) {
    routine.cancel()
  }

  /**
   * Exercise: Implement a self-closing actor.
   * - If the actor receives a message (anything), you have 1 second to send it another message.
   * - If the time window expires, the actor will stop itself.
   * - If you send another message, the time window is reset.
   */

  class SelfClosingActor extends Actor with ActorLogging {
    var schedule = createTimeoutWindow()

    override def receive: Receive = {
      case "timeout" =>
        log.info(s"Stopping myself")
        context.stop(self)
      case message =>
        log.info(s"Received message: $message... staying alive.")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }

    def createTimeoutWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }
  }

}
