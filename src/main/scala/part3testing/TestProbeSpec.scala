package part3testing

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}

object TestProbeSpec {
  // scenario
  /*
    Word counting Actor hierarchy (master-slave)
    Send work to master
    -> Master sends the slave the piece of work
    -> Slave processes the work and replies to master
    -> Master aggregates the result
    Master sends the total count to the original requester.
   */

  case class Register(slaveRef: ActorRef)
  case class Work(text: String)
  case class SlaveWork(text: String, originalRequester: ActorRef)
  case class WorkCompleted(count: Int, originalRequester: ActorRef)
  case class Report(totalCount: Int)

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) => context.become(online(slaveRef, 0))
      case _ => // ignore
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalWordCount = totalWordCount + count
        originalRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }
  }

  // class Slave extends Actor...

}
