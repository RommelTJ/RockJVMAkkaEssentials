package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class AskSpec extends TestKit(ActorSystem("AskSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskSpec._

}

object AskSpec {

  // this code is somewhere else in your application
  case class Read(key: String)
  case class Write(key: String, value: String)
  class KVActor extends Actor with ActorLogging {
    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Reading value at key: $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"Writing value: $value for key: $key")
        context.become(online(kv + (key -> value)))
    }

  }

}
