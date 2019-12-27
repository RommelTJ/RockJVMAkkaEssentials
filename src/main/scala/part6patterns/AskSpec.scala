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
import scala.util.{Failure, Success}

class AskSpec extends TestKit(ActorSystem("AskSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskSpec._
  import AuthManager._

  "An authenticator" should {
    "fail to authenticate a non-registered user" in {
      val authManager = system.actorOf(Props[AuthManager])
      authManager ! Authenticate("rommel", "password")
      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
    }

    "fail to authenticate if invalid password" in {
      val authManager = system.actorOf(Props[AuthManager])
      authManager ! RegisterUser("rommel", "password")
      authManager ! Authenticate("rommel", "wrong_password")
      expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INVALID))
    }
  }

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
    import AuthManager._

    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    private val authDb = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authDb ! Write(username, password)
      case Authenticate(username, password) => handleAuthentication(username, password)
    }

    def handleAuthentication(username: String, password: String): Unit = {
      val originalSender = sender()
      val future = authDb ? Read(username)
      future.onComplete {
        // NEVER CALL METHODS ON THE ACTOR INSTANCE OR ACCESS MUTABLE STATE IN ONCOMPLETE.
        // Avoid closing over the actor instance or mutable state
        case Success(None) =>
          originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(dbPassword)) =>
          if (dbPassword == password) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_PASSWORD_INVALID)
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
      }
    }

  }

  object AuthManager {
    val AUTH_FAILURE_NOT_FOUND = "Username not found"
    val AUTH_FAILURE_PASSWORD_INVALID = "Password incorrect"
    val AUTH_FAILURE_SYSTEM = "System error"
  }

}
