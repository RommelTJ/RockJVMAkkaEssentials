package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello, there!" // replying to a message
      case message: String => println(s"[${context.self.path.name}] I have received $message")
      case number: Int => println(s"[${self.path.name}] I have received a number: $number")
      case SpecialMessage(contents) =>  println(s"[simple actor] I have received something special: $contents")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep the original sender of the wireless phone message
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "hello, actor"

  // 1 - messages can be of any type
  // Rules:
  // a) Messages must be IMMUTABLE
  // b) Messages must be SERIALIZABLE
  // In practice, just use case classes and case objects.
  simpleActor ! 42
  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("Some special content")

  // 2 - Actors have information about their context and about themselves.
  // context.self is the equivalent to "this" in OOP.
  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it.")

  // 3 - How actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // 4 - Dead Letters. The Garbage Pool of Akka.
  alice ! "Hi!" // reply to me, but I am null. Message to Actor from Actor was not delivered. Dead Letters.

  // 5 - Forwarding Messages. Sending a message with the ORIGINAL sender.
  // D -> A -> B
  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob)

  /**
   * Exercises
   * 1. Create a Counter Actor
   * - Increment
   * - Decrement
   * - Print
   *
   * 2. Create a Bank Account as an Actor
   * Receives
   * - Deposit an amount
   * - Withdraw an Amount
   * - Statement
   * Replies
   * - Reply with Success/Failure
   * Interact with some other kind of actor
   */

  // Exercise 1
  val counterSystem = ActorSystem("counterSystem")
  class CounterActor extends Actor {
    import CounterActor._
    var count: Int = 0
    override def receive: Receive = {
      case CounterIncrement(num) => count += num
      case CounterDecrement(num) => count -= num
      case CounterPrint => println(s"Count is $count")
    }
  }
  // Domain of Counter
  object CounterActor {
    case class CounterIncrement(value: Int)
    case class CounterDecrement(value: Int)
    case object CounterPrint
  }
  val counterActor = counterSystem.actorOf(Props[CounterActor], "counterActor")
  counterActor ! CounterActor.CounterIncrement(1)
  counterActor ! CounterActor.CounterIncrement(2)
  counterActor ! CounterActor.CounterIncrement(3)
  counterActor ! CounterActor.CounterPrint
  counterActor ! CounterActor.CounterDecrement(2)
  counterActor ! CounterActor.CounterPrint

  // Exercise 2
  val bankAccountSystem = ActorSystem("bankAccountSystem")
  class BankAccountActor extends Actor {
    import BankAccountActor._
    var accountBalance: Int = 0

    override def receive: Receive = {
      case AccountDeposit(num) =>
        if (num < 0) sender() ! AccountTransactionFailure("Invalid Deposit Amount")
        else {
          accountBalance += num
          sender() ! AccountTransactionSuccess(s"Successfully deposited $$$num")
        }
      case AccountWithdraw(num) =>
        if (num < 0) sender() ! AccountTransactionFailure("Invalid Withdraw Amount")
        else if (num > accountBalance) sender() ! AccountTransactionFailure("Insufficient funds")
        else {
          accountBalance -= num
          sender() ! AccountTransactionSuccess(s"Successfully withdrew $$$num")
        }
      case AccountStatement => s"Your Account Balance is: $$${accountBalance}"
    }
  }
  object BankAccountActor {
    case class AccountDeposit(amount: Int)
    case class AccountWithdraw(amount: Int)
    case object AccountStatement
    case class AccountTransactionSuccess(message: String)
    case class AccountTransactionFailure(message: String)
  }
  val bankAccountActor = bankAccountSystem.actorOf(Props[BankAccountActor], "bankAccountActor")
  bankAccountActor ! BankAccountActor.AccountDeposit(500)
  bankAccountActor ! BankAccountActor.AccountWithdraw(20)
  bankAccountActor ! BankAccountActor.AccountStatement

}
