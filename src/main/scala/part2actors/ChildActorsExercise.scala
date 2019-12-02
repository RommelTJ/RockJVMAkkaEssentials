package part2actors

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}

object ChildActorsExercise extends App {

  // Distributed Word Counting
  val system = ActorSystem("ChildActorsExercise")

  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        println(s"[MASTER] Creating $nChildren children.")
        for (n <- 1 to nChildren) {
          context.actorOf(Props[WordCounterWorker], s"wcw$n")
        }
    }

    def withChild(childRef: ActorRef): Receive = {
      case WordCountTask(task) => childRef forward task
    }
  }

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(/* TODO */text: String)
    case class WordCountReply(/* TODO */count: Int)
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(task) => println(task)
      case WordCountReply(count) => println(count)
    }
  }

  /*
  Flow of exercise:
  Create a WordCounterMaster
  Send Initialize(10) to WordCounterMaster -> Create 10 workers
  Send "Akka is awesome" to wordCounterMaster
  -> WCM will send a WordCountTask("...") to one of its children
   -> Child replies with a WordCountReply(3) to the master
  -> Master replies with 3 to the sender

  requester -> WCM -> WCW
          r <- WCM <-
   */

  // Round-robin logic
  // 1, 2, 3, 4, 5 and 7 tasks -> 1, 2, 3, 4, 5, 1, 2

  // Hint: You might need to pass some extra information to WordCountTask and WordCountReply.

}
