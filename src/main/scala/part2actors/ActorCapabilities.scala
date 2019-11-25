package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[simple actor] I have received $message")
      case number: Int => println(s"[simple actor] I have received a number: $number")
      case SpecialMessage(contents) =>  println(s"[simple actor] I have received something special: $contents")
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "hello, actor"

  // 1 - messages can be of any type
  // Rules:
  // a) Messages must be IMMUTABLE
  // b) Messages must be SERIALIZABLE
  // In practice, just use case classes and case objects.
  simpleActor ! 42
  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("Some special content")

}
