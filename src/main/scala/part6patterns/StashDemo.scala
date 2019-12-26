package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {

  /**
   * ResourceActor
   * - open => it can receive read/write requests to the resource
   * - otherwise => it will postpone all read/write requests until the state is open
   *
   * ResourceActor starts closed
   * - open => Switch to Open state
   * - Read, Write messages are postponed
   *
   * ResourceActor is open
   * - Read, Write are handled
   * - Close => Switch to Closed state
   *
   * [Open, Read, Read, Write]
   * - Switch to Open state
   * - Read the data
   * - Read the data
   * - Write the data
   *
   * [Read, Open, Write]
   * - Stash Read
   * - Stash: [Read]
   * - Switch to Open state
   * - Mailbox: [Read, Write]
   * - Read the data
   * - Write the data
   *
   */
  case object Open
  case object Closed
  case object Read
  case class Write(data: String)

  // Step 1 - Mix-in the Stash trait
  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info(s"Opening resource...")
        // Step 3 - Before doing context become, you do unstashAll()
        unstashAll()
        context.become(open)
      case message =>
        log.info(s"Stashing $message because I can't handle it in the closed state...")
        // Step 2 - Stash away messages that you cannot handle.
        stash()
    }

    def open: Receive = {
      case Read =>
        log.info(s"I have read: $innerData")
      case Write(data) =>
        log.info(s"I am writing data: $data")
        innerData = data
      case Closed =>
        log.info(s"Closing resource...")
        unstashAll()
        context.become(closed)
      case message =>
        log.info(s"Stashing $message because I can't handle it in the open state...")
        stash()
    }
  }

  val system = ActorSystem("StashDemo")
  val resourceActor = system.actorOf(Props[ResourceActor], "resourceActor")
  resourceActor ! Write("I love stash")
  resourceActor ! Read
  resourceActor ! Open
  resourceActor ! Write("Writing something else")
  resourceActor ! Read
  resourceActor ! Read

}
