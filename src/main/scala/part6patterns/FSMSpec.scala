package part6patterns

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, FSM, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class FSMSpec extends TestKit(ActorSystem("FSMSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import FSMSpec._

  "A vending machine" should {
    "error when not initialized" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! RequestProduct("coke")
      expectMsg(VendingError("MachineNotInitialized"))
    }

    "report a product is not available" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(inventory = Map("coke" -> 10), prices = Map("coke" -> 1))
      vendingMachine ! RequestProduct("sandwich")
      expectMsg(VendingError("ProductNotAvailable"))
    }

    "throw a timeout if I don't insert money" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(inventory = Map("coke" -> 10), prices = Map("coke" -> 1))

      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction(s"Please insert 1 dollars"))

      within(1.5 seconds) {
        expectMsg(VendingError("RequestTimedOut"))
      }
    }

    "handle the reception of partial money" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(inventory = Map("coke" -> 10), prices = Map("coke" -> 3))

      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction(s"Please insert 3 dollars"))

      vendingMachine ! ReceiveMoney(1)
      expectMsg(Instruction("Please insert 2 dollars"))

      within(1.5 seconds) {
        expectMsg(VendingError("RequestTimedOut"))
        expectMsg(GiveBackChange(1))
      }
    }

    "deliver the product if I insert all the money" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(inventory = Map("coke" -> 10), prices = Map("coke" -> 3))

      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction(s"Please insert 3 dollars"))

      vendingMachine ! ReceiveMoney(3)
      expectMsg(Deliver("coke"))
    }

    "give back change and be able to request money for a new product" in {
      val vendingMachine = system.actorOf(Props[VendingMachine])
      vendingMachine ! Initialize(inventory = Map("coke" -> 10), prices = Map("coke" -> 3))

      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction("Please insert 3 dollars"))

      vendingMachine ! ReceiveMoney(4)
      expectMsg(Deliver("coke"))
      expectMsg(GiveBackChange(1))

      vendingMachine ! RequestProduct("coke")
      expectMsg(Instruction("Please insert 3 dollars"))
    }

  }

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
    implicit val executionContext: ExecutionContext = context.dispatcher

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
              waitForMoney(inventory, prices, product, 0, startReceiveMoneyTimeoutSchedule, sender())
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
    ): Receive = {
      case ReceiveMoneyTimeout =>
        requester ! VendingError("RequestTimedOut")
        if (money > 0) requester ! GiveBackChange(money)
        context.become(operational(inventory, prices))
      case ReceiveMoney(amount) =>
        moneyTimeoutSchedule.cancel()
        val price = prices(product)
        if (money + amount >= price) {
          // User buys product
          requester ! Deliver(product)

          // Deliver the change
          if (money + amount - price > 0) requester ! GiveBackChange(money + amount - price)

          // Update the inventory
          val newStock = inventory(product) - 1
          val newInventory = inventory + (product -> newStock)
          context.become(operational(newInventory, prices))
        } else {
          val remainingMoney = price - money - amount
          requester ! Instruction(s"Please insert $remainingMoney dollars")
          context.become(
            waitForMoney(inventory, prices, product, money + amount, startReceiveMoneyTimeoutSchedule, requester)
          )
        }
    }

    def startReceiveMoneyTimeoutSchedule: Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! ReceiveMoneyTimeout
      }
    }

  }

  /**
   * Rewriting VendingMachine as a Finite-State Machine.
   */
  // Step 1 - Define the states and the data of the actor
  trait VendingState
  case object Idle extends VendingState
  case object Operational extends VendingState
  case object WaitForMoney extends VendingState

  trait VendingData
  case object Uninitialized extends VendingData
  case class Initialized(inventory: Map[String, Int], prices: Map[String, Int]) extends VendingData
  case class WaitForMoneyData(
    inventory: Map[String, Int],
    prices: Map[String, Int],
    product: String,
    money: Int,
    requester: ActorRef
  ) extends VendingData

  class VendingMachineFSM extends FSM[VendingState, VendingData] {
    // We don't have a receive handler.
    // Triggers an EVENT(Message, Data)

    /**
     * An FSM is an Actor with a state and data
     * event => (state, data) can be changed.
     *
     * Ex:
     * state = Idle
     * data = Uninitialized
     * event(Initialize(Map(coke -> 10), Map(coke -> 1))) =>
     *   state = Operational
     *   data = Initialized(Map(coke -> 10), Map(coke -> 1))
     *
     * Ex:
     * event(RequestProduct(coke)) =>
     *   state = WaitForMoney
     *   data = WaitForMoneyData(Map(coke -> 10), Map(coke -> 1), coke, 0, R)
     */

    startWith(Idle, Uninitialized)

    when(Idle) {
      case Event(Initialize(inventory, prices), Uninitialized) =>
        goto(Operational) using Initialized(inventory, prices) // equivalent to context.become(operational(inv, pr))
      case _ =>
        sender() ! VendingError("MachineNotInitializedError")
        stay()
    }

    when(Operational) {
      case Event(RequestProduct(product), Initialized(inventory, prices)) =>
        inventory.get(product) match {
          case None | Some(0) =>
            sender() ! VendingError("ProductNotAvailable")
            stay()
          case Some(_) =>
            val price = prices(product)
            sender() ! Instruction(s"Please insert $price dollars")
            goto(WaitForMoney) using WaitForMoneyData(inventory, prices, product, 0, sender())
        }
    }

    when(WaitForMoney) {
      case Event(ReceiveMoney(amount), WaitForMoneyData(inventory, prices, product, money, requester)) =>
        val price = prices(product)
        if (money + amount >= price) {
          // User buys product
          requester ! Deliver(product)

          // Deliver the change
          if (money + amount - price > 0)
            requester ! GiveBackChange(money + amount - price)

          // Update the inventory
          val newStock = inventory(product) - 1
          val newInventory = inventory + (product -> newStock)
          goto(Operational) using Initialized(newInventory, prices)
        } else {
          val remainingMoney = price - money - amount
          requester ! Instruction(s"Please insert $remainingMoney dollars")
          stay() using WaitForMoneyData(inventory, prices, product, money + amount, requester)
        }
    }

  }

}
