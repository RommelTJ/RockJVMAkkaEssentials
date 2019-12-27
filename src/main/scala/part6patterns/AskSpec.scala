package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

// Step 1 - Import the methods
import akka.pattern.ask
import akka.pattern.pipe

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

    "successfully authenticate a registered user" in {
      val authManager = system.actorOf(Props[AuthManager])
      authManager ! RegisterUser("rommel", "password")
      authManager ! Authenticate("rommel", "password")
      expectMsg(AuthSuccess)
    }
  }

  "A piped authenticator" should {
    "fail to authenticate a non-registered user" in {
      val authManager = system.actorOf(Props[PipedAuthManager])
      authManager ! Authenticate("rommel", "password")
      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
    }

    "fail to authenticate if invalid password" in {
      val authManager = system.actorOf(Props[PipedAuthManager])
      authManager ! RegisterUser("rommel", "password")
      authManager ! Authenticate("rommel", "wrong_password")
      expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INVALID))
    }

    "successfully authenticate a registered user" in {
      val authManager = system.actorOf(Props[PipedAuthManager])
      authManager ! RegisterUser("rommel", "password")
      authManager ! Authenticate("rommel", "password")
      expectMsg(AuthSuccess)
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

    // Step 2 - Logistics
    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDb = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authDb ! Write(username, password)
      case Authenticate(username, password) => handleAuthentication(username, password)
    }

    def handleAuthentication(username: String, password: String): Unit = {
      val originalSender = sender()
      // Step 3 - Ask the Actor
      val future = authDb ? Read(username)
      // Step 4 - Handle the future for e.g. with onComplete
      future.onComplete {
        // Step 5 - Most important
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

  class PipedAuthManager extends AuthManager {
    import AuthManager._

    override def handleAuthentication(username: String, password: String): Unit = {
      // Step 3 - Ask the Actor
      val future = authDb ? Read(username) // Future[Any]
      // Step 4 - Process the future until you get the responses you will send back
      val passwordFuture = future.mapTo[Option[String]] // Future[Option[String]]
      val responseFuture = passwordFuture.map {
        case None => AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Some(dbPassword) =>
          if (dbPassword == password) AuthSuccess
          else AuthFailure(AUTH_FAILURE_PASSWORD_INVALID)
      } // Future[Any], but will be completed with Future[AuthSuccess/AuthFailure]

      // Step 5 - Pipe the resulting future to the actor you want to send the result to.
      /**
       * When the future completes, send the response to the actor ref in the arg list.
       * Piping the result doesn't expose you to onComplete callbacks where you can break the
       * actor encapsulation.
       */
      responseFuture.pipeTo(sender())
    }
  }

}
