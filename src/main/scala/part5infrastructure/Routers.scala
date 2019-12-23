package part5infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinPool, RoundRobinRoutingLogic, Router}

object Routers extends App {

  /*
  Method #1 - Manual Router
   */
  class Master extends Actor {
    // Step 1 - Create Routees.
    // 5 actor routees based off Slave Actors
    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    // Step 2 - Define Router
    private var router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      // Step 4 - handle the termination/lifecycle of the routees
      case Terminated(ref) =>
        router = router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
      // Step 3 - Route the messages
      case message =>
        router.route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo" /* TODO: Config */)
  val master = system.actorOf(Props[Master], "master")

//  for (i <- 1 to 10) {
//    master ! s"[$i] Hello from the world"
//  }

  /*
  Method #2 - Router Actor with its own Children
  POOL router
   */
  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")
  for (i <- 1 to 10) {
    poolMaster ! s"[$i] Hello from the world"
  }

}
