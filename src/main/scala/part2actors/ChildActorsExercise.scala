package part2actors

import akka.actor.Actor

object ChildActorsExercise extends App {

  // Distributed Word Counting

  class WordCounterMaster extends Actor {
    override def receive: Receive = ???
  }
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = ???
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

}
