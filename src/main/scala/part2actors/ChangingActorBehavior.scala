package part2actors

import akka.actor.Actor

object ChangingActorBehavior extends App {

  class FuzzyKid extends Actor {
    import FuzzyKid._
    import Mom._

    // internal state of the kid
    var state: String = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(message) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
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
