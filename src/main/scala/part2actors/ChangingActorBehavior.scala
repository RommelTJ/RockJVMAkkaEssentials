package part2actors

import akka.actor.Actor

object ChangingActorBehavior extends App {

  class FuzzyKid extends Actor {
    override def receive: Receive = ???
  }

  class Mom extends Actor {
    override def receive: Receive = ???
  }

}
