package part6patterns

import akka.actor.{Actor, ActorLogging, Stash}

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
  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = ???

    def open: Receive = ???
  }





}
