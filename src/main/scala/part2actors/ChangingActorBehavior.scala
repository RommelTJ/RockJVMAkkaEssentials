package part2actors

import akka.actor.Actor

object ChangingActorBehavior extends App {

  class FuzzyKid extends Actor {
    override def receive: Receive = ???
  }
  object FuzzyKid {
    case object KidAccept
    case object KidReject
  }

  class Mom extends Actor {
    override def receive: Receive = ???
  }
  object Mom {
    case class Food(food: String)
    case class Ask(message: String) // do you want to play?
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

}
