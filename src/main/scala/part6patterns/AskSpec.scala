package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

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

  // User authenticator actor
  case class RegisterUser(username: String, password: String)
  case class Authenticate(username: String, password: String)
  case class AuthFailure(message: String)
  case object AuthSuccess

  class AuthManager extends Actor with ActorLogging {
    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    private val authDb = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) =>
        authDb ! Write(username, password)
      case Authenticate(username, password) =>
        val future = authDb ? Read(username)
        future.onComplete {
          case Success(None) =>
            sender() ! AuthFailure("Username not found")
          case Success(Some(dbPassword)) =>
            if (dbPassword == password) sender() ! AuthSuccess
            else sender() ! AuthFailure("Password incorrect.")
        }
    }
  }

}
