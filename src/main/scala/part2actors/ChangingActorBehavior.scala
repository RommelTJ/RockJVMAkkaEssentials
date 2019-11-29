package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Mom.MomStart

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

  class StatelessFuzzyKid extends Actor {
    import FuzzyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false) // change my receive handler to sadReceive
      case Food(CHOCOLATE) => // stay happy
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.unbecome() // pop from the stack of handlers
      case Ask(_) => sender() ! KidReject
    }
  }

  class Mom extends Actor {
    import Mom._
    import FuzzyKid._

    override def receive: Receive = {
      case MomStart(kid) =>
        // test our interaction
        kid ! Food(VEGETABLE)
        kid ! Food(VEGETABLE)
        kid ! Food(CHOCOLATE)
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

  val system = ActorSystem("changingActorBehaviorDemo")
  val fuzzyKid = system.actorOf(Props[FuzzyKid], "fuzzyKid")
  val statelessFuzzyKid = system.actorOf(Props[StatelessFuzzyKid], "statelessFuzzyKid")
  val mom = system.actorOf(Props[Mom], "mom")

  mom ! MomStart(statelessFuzzyKid)

  /*
  Notes:
  mom receives MomStart
  - kid receives Food(veg) -> kid will change handler to sadReceive
  - kid receives Ask(play?) -> kid replies with sadReceive handler
  - mom receives KidReject
   */

  /*
  Assume you sent (using discardOld = false)
  Food(veg) -> stack.push(sadReceive)
  Food(chocolate) -> stack.push(happyReceive)

  Stack:
  1. happyReceive
  2. sadReceive
  3. happyReceive
   */

  /*
  New Behavior:
  Food(veg)
  Food(veg)
  Food(chocolate)
  Food(chocolate)

  Stack:
  1. happyReceive
   */

}
