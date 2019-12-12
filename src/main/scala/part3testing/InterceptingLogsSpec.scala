package part3testing

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class InterceptingLogsSpec extends TestKit(ActorSystem("InterceptingLogsSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}

object InterceptingLogsSpec {

  class CheckoutActor extends Actor {
    override def receive: Receive = ???
  }

  class PaymentManager extends Actor {
    override def receive: Receive = ???
  }

  class FulfillmentManager extends Actor {
    override def receive: Receive = ???
  }

}