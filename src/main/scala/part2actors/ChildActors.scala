package part2actors

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

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

  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child1")
  parent ! TellChild("hey kid!")

  // Actor hierarchies
  // Parent -> Child -> Grandchild
  //        -> Child2

  /*
  Guardian Actors (top-level) Who owns parent? Guardian Actors.
  - /system = system guardian
  - /user = user-level guardian
  - / = root-level guardian. Root guardian manages /system and /user.
   */

  /**
   * Actor Selection
   */
  val childSelection: ActorSelection = system.actorSelection("/user/parent/child1")
  childSelection ! "Wut wut"

  // Invalid path
  val invalidSelection: ActorSelection = system.actorSelection("/user/parent/dsfhdegj")
  invalidSelection ! "dfg" // Sent to dead-letters

  /**
   * Danger!
   * NEVER PASS MUTABLE ACTOR STATE OR THE `THIS` REFERENCE TO CHILD ACTORS. NEVER IN YOUR LIFE.
   * This has the danger of breaking actor encapsulation. Because child actor suddenly has access to the internals
   * of the parent actor, so it can mutate the state or directly call methods of the parent actor without sending a
   * message and this breaks our very sacred actor principles.
   */

  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    
    override def receive: Receive = ???
  }
  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object InitializeAccount
  }

}
