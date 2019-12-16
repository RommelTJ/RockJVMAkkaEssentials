package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifecycle extends App {

  object StartChild
  class LifecycleActor extends Actor with ActorLogging {
    override def preStart(): Unit = log.info(s"I am starting...")

    override def postStop(): Unit = log.info(s"I have stopped...")

    override def receive: Receive = {
      case StartChild => context.actorOf(Props[LifecycleActor], "child")
    }
  }

  val system = ActorSystem("LifecycleDemo")
  val parent = system.actorOf(Props[LifecycleActor], "parent")
  parent ! StartChild
  parent ! PoisonPill

  /**
   * Restart
   */
  object Fail
  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case Fail =>
        log.info(s"child will fail now...")
        throw new RuntimeException("I failed...")
    }
  }

}
