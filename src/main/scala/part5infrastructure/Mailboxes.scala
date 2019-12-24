package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

object Mailboxes extends App {

  val system = ActorSystem("MailboxDemo")

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * Interesting Case #1 - Custom priority mailbox
   * P0 -> most important
   * P1
   * P2
   * P3 -> least important
   */
  // Step 1 - Mailbox definition
  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message.startsWith("[P0]") => 0
        case message: String if message.startsWith("[P1]") => 1
        case message: String if message.startsWith("[P2]") => 2
        case message: String if message.startsWith("[P3]") => 3
        case _ => 4
      }
    )

  // Step 2 - Make it known in the configuration

  // Step 3 - Attach the dispatcher to an actor
  val supportTicketLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  supportTicketLogger ! PoisonPill
  Thread.sleep(1000) // If you do this, the messages will be sent to dead letters.
  // This happens because after which time can I send another message and be prioritized accordingly? You cannot know
  // or configure the wait. Whatever is put on the queue will get handled.
  supportTicketLogger ! "[P3] this thing would be nice to have"
  supportTicketLogger ! "[P0] this needs to be solved now"
  supportTicketLogger ! "[P1] do this when you have the time"

}
