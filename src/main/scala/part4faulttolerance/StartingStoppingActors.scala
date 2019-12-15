package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props}

object StartingStoppingActors extends App {

  val system = ActorSystem("StoppingActorsDemo")

  class Parent extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = withChildren(children = Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"Starting child with name: $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"Stopping child with name: $name")
        val childOption = children.get(name)
        childOption.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info(s"Stopping myself")
        context.stop(self)
      case message =>
        log.info(message.toString)
    }
  }
  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  import Parent._
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("child1")
  val child = system.actorSelection("/user/parent/child1")
  child ! "Hi kid!"

  parent ! StopChild("child1")
  // Note: Child stops asynchronously, so not immediately.
  // for (_ <- 1 to 50) child ! "are you still there?" // Will receive a few of those before DeadLetters.

  parent ! StartChild("child2")
  val child2 = system.actorSelection("/user/parent/child2")
  child2 ! "Hi, Second child!"
  parent ! Stop
  // for (i <- 1 to 10) parent ! s"[$i]Parent, are you still there?" // Are never received because Stop is sent first.
  // for (i <- 1 to 100) child2 ! s"[$i]Child, are you still there?" // Some are received.

  /**
   * Method #2 for stopping Actors - Using Special Messages
   */
  val looseActor = system.actorOf(Props[Child])
  looseActor ! "Hello, loose actor"
  looseActor ! PoisonPill
  looseActor ! "Are you still there, loose actor?"

  val abruptlyTerminatedActor = system.actorOf(Props[Child])
  abruptlyTerminatedActor ! "hi, abruptly terminated actor"
  abruptlyTerminatedActor ! Kill
  abruptlyTerminatedActor ! "abruptly still there?"

  /**
   * Death watch
   */
  class Watcher extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Started and watching child with name: $name")
        context.watch(child)
    }
  }

}
