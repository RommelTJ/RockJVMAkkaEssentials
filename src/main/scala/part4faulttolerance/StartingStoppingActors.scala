package part4faulttolerance

import akka.actor.{Actor, ActorSystem}

object StartingStoppingActors extends App {

  val system = ActorSystem("StoppingActorsDemo")

  class Parent extends Actor {
    override def receive: Receive = ???
  }

  class Child extends Actor {
    override def receive: Receive = ???
  }

}
