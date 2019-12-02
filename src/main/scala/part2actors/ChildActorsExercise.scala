package part2actors

import akka.actor.Actor

object ChildActorsExercise extends App {

  // Distributed Word Counting

  class WordCounterMaster extends Actor {
    override def receive: Receive = ???
  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = ???
  }

}
