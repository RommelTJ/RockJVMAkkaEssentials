package part4faulttolerance

import akka.actor.{Actor, ActorLogging, Props}

object ActorLifecycle extends App {

  object StartChild
  class LifecycleActor extends Actor with ActorLogging {
    override def preStart(): Unit = log.info(s"I am starting...")

    override def postStop(): Unit = log.info(s"I have stopped...")
    
    override def receive: Receive = {
      case StartChild => context.actorOf(Props[LifecycleActor], "child")
    }
  }

}
