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

  /**
   * Exercises
   * 1 - Recreate the CounterActor with context.become and no mutable state.
   * 2 - Simplified voting system.
   * -> Print the status of the votes. Map of candidate and number of votes they received.
   * -> // Martin -> 1, Jonas -> 1, Roland -> 2
   */

  // Solution Exercise 1

  class StatelessCounterActor extends Actor {
    import StatelessCounterActor._

    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment => context.become(countReceive(currentCount + 1))
      case Decrement => context.become(countReceive(currentCount - 1))
      case Print => println(s"Current Count is: $currentCount")
    }

  }
  object StatelessCounterActor {
    case object Increment
    case object Decrement
    case object Print
  }

  val statelessCounterActor = system.actorOf(Props[StatelessCounterActor])
  statelessCounterActor ! StatelessCounterActor.Increment
  statelessCounterActor ! StatelessCounterActor.Increment
  statelessCounterActor ! StatelessCounterActor.Print
  statelessCounterActor ! StatelessCounterActor.Decrement
  statelessCounterActor ! StatelessCounterActor.Decrement
  statelessCounterActor ! StatelessCounterActor.Decrement
  statelessCounterActor ! StatelessCounterActor.Decrement
  statelessCounterActor ! StatelessCounterActor.Decrement
  statelessCounterActor ! StatelessCounterActor.Print

  // Exercise 2
  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) =>
        // a citizen hasn't voted yet
        sender() ! VoteStatusRequest // this might end up in an infinite loop
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $newStats")
        } else {
          // still need to process some statuses
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

}
