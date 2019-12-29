package part6patterns

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class FSMSpec extends TestKit(ActorSystem("FSMSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import FSMSpec._

}

object FSMSpec {

  /**
   * Vending machine
   */
  case class Initialize(inventory: Map[String, Int], prices: Map[String, Int])
  case class RequestProduct(product: String)

  case class Instruction(instruction: String) // message the vending machine will show on its "screen"
  case class ReceiveMoney(amount: Int)
  case class Deliver(product: String)
  case class GiveBackChange(amount: Int)

  case class VendingError(reason: String)
  case object ReceiveMoneyTimeout

  class VendingMachine extends Actor with ActorLogging {
    override def receive: Receive = idle

    def idle: Receive = {
      case Initialize(inv, prices) => context.become(operational(inv, prices))
      case _ => sender() ! VendingError("MachineNotInitialized")
    }

    def operational(inventory: Map[String, Int], prices: Map[String, Int]): Receive = {
      case RequestProduct(product) =>
        inventory.get(product) match {
          case None | Some(0) =>
            sender() ! VendingError("ProductNotAvailable")
          case Some(_) =>
            val price = prices(product)
            sender() ! Instruction(s"Please insert $price dollars")
            context.become(
              waitForMoney(inventory, prices, product, price, ???, sender())
            )
        }
    }

    def waitForMoney(
      inventory: Map[String, Int],
      prices: Map[String, Int],
      product: String,
      money: Int,
      moneyTimeoutSchedule: Cancellable,
      requester: ActorRef
    ): Receive = ???
  }

}
