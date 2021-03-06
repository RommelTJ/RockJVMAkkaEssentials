package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object Dispatchers extends App {

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] ${message.toString}")
    }
  }

  val system = ActorSystem("DispatchersDemo") // , ConfigFactory.load().getConfig("dispatchersDemo")

  // Method #1 - Programmatic.
  val actors = for (i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
//  val r = new Random()
//  for (i <- 1 to 1000) {
//    actors(r.nextInt(10)) ! i
//  }

  // Method #2 - From Config.
  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")
//  val r = new Random()
//  for (i <- 1 to 1000) {
//    actors(r.nextInt(10)) ! i
//  }

  /**
   * Dispatchers implement the ExecutionContext trait
   */
  class DBActor extends Actor with ActorLogging {
    // Solution #1 to blocking future is to use a different execution context.
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher") // context.dispatcher
    // Solution #2 - Use Router

    override def receive: Receive = {
      case message =>
        Future {
          // hard computation / wait on resource
          Thread.sleep(5000)
          log.info(s"Success: $message")
        }
    }
  }

  val dbActor = system.actorOf(Props[DBActor])
//  dbActor ! "The meaning of life is 42"

  val nonBlockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000) {
    val message = s"Important message $i"
    dbActor ! message
    nonBlockingActor ! message
  }

}
