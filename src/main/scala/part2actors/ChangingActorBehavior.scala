package part2actors

import akka.actor.Actor

object ChangingActorBehavior extends App {

  class FuzzyKid extends Actor {
    import FuzzyKid._

    // internal state of the kid
    var state = HAPPY

    override def receive: Receive = ???
  }
  object FuzzyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
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
