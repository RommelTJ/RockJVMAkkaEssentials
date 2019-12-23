package part5infrastructure

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

object Routers extends App {

  /*
  Method #1 - Manual Router
   */
  class Master extends Actor {
    // Step 1 - Create Routees.
    // 5 actor routees based off Slave Actors
    private val slaves = for (_ <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave])
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    // Step 2 - Define Router
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    // Step 3 - Route the messages
    override def receive: Receive = {
      case message =>
        router.route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

}
