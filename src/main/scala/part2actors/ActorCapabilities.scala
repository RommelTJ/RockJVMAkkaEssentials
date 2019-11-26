package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[${context.self.path.name}] I have received $message")
      case number: Int => println(s"[${self.path.name}] I have received a number: $number")
      case SpecialMessage(contents) =>  println(s"[simple actor] I have received something special: $contents")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi!"
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

  // 2 - Actors have information about their context and about themselves.
  // context.self is the equivalent to "this" in OOP.
  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it.")

  // 3 - How actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

}
