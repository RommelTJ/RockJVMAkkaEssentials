package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.TestActors.BlackholeActor
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import part3testing.BasicSpec.SimpleActor
import scala.concurrent.duration._
import scala.language.postfixOps

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  // Setup
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val msg = "hello, test"
      echoActor ! msg
      expectMsg(msg) // duration of waiting before failing can be configured with: akka.test.single-expect-default
    }
  }

  "A black hole actor" should {
    "not send back some message" in {
      val blackholeActor = system.actorOf(Props[BlackholeActor])
      val msg = "hello, test"
      blackholeActor ! msg
      expectNoMessage(max = 1 second)
    }
  }

}

object BasicSpec {
  // Use this to contain all the common and shared values to be used in the test.

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

}