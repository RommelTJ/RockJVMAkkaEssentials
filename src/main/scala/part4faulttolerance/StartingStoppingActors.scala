package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem}

object StartingStoppingActors extends App {

  val system = ActorSystem("StoppingActorsDemo")

  class Parent extends Actor {
    override def receive: Receive = ???
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

}
