package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
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

  implicit val executionContext = system.dispatcher
  system.scheduler.scheduleOnce(delay = 1 second) {
    simpleActor ! "reminder"
  }

}
