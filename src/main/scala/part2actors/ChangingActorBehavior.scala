package part2actors

import akka.actor.{Actor, ActorRef}

object ChangingActorBehavior extends App {

  class FuzzyKid extends Actor {
    import FuzzyKid._
    import Mom._

    // internal state of the kid
    var state: String = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
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
    import Mom._
    import FuzzyKid._

    override def receive: Receive = {
      case MomStart(kid) =>
        // test our interaction
        kid ! Food(VEGETABLE)
        kid ! Ask("Do you want to play?")
      case KidAccept => println("Yay! My kid is happy!")
      case KidReject => println("My kid is sad, but at least he's healthy!")
    }
  }
  object Mom {
    case class MomStart(kid: ActorRef)
    case class Food(food: String)
    case class Ask(message: String) // do you want to play?
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

}
