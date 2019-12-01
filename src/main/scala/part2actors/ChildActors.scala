package part2actors

import akka.actor.Actor

object ChildActors extends App {

  // Actors can create other Actors

  class Parent extends Actor {
    import Parent._

    override def receive: Receive = ???
  }
  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} - I got: $message")
    }
  }

}
