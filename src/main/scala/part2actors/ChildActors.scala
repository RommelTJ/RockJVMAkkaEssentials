package part2actors

import akka.actor.{Actor, ActorRef, Props}

object ChildActors extends App {

  // Actors can create other Actors

  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} - Creating child")
        // How to create a new Actor
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
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
