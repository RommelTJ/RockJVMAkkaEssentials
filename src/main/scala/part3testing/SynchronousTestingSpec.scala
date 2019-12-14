package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.TestActorRef
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import part3testing.SynchronousTestingSpec.{Counter, Inc, Read}

class SynchronousTestingSpec extends AnyWordSpecLike with BeforeAndAfterAll {

  implicit val system = ActorSystem("SynchronousTestingSpec")

  override protected def afterAll(): Unit = {
    system.terminate()
  }

  "A counter" should {
    "synchronously increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter ! Inc // counter has ALREADY received the message
      assert(counter.underlyingActor.count == 1)
    }

    "synchronously increase its counter at the call of the receive function" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Inc)
      assert(counter.underlyingActor.count == 1)
    }
  }

}

object SynchronousTestingSpec {

  case object Inc
  case object Read

  class Counter extends Actor {
    var count = 0

    override def receive: Receive = {
      case Inc => count += 1
      case Read => sender() ! count
    }
  }

}
