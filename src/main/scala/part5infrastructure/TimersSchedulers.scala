package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

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
//  system.log.info(s"Scheduling reminder for simpleActor")

  import system.dispatcher
//  system.scheduler.scheduleOnce(delay = 1 second) {
//    simpleActor ! "reminder"
//  }
//
//  val routine: Cancellable = system.scheduler.schedule(initialDelay = 1 second, interval = 2 seconds) {
//    simpleActor ! "heartbeat"
//  }
//
//  system.scheduler.scheduleOnce(5 seconds) {
//    routine.cancel()
//  }

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

//  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
//  system.scheduler.scheduleOnce(250 millis) {
//    selfClosingActor ! "ping"
//  }
//  system.scheduler.scheduleOnce(2 seconds) {
//    system.log.info(s"Sending pong to the self-closing actor")
//    selfClosingActor ! "pong"
//  }

  /**
   * Timer: Used to send messages to yourself.
   */
  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedScheduleActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(key = TimerKey, msg = Start, timeout = 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info(s"Bootstrapping")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info(s"I am alive.")
      case Stop =>
        log.warning("Stopping!")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerBasedScheduleActor = system.actorOf(Props[TimerBasedScheduleActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerBasedScheduleActor ! Stop
  }

}
