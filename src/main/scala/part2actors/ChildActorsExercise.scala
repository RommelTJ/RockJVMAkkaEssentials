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

}
