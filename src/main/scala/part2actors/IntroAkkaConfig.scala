package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  // Method 1 - Inline Configuration
  val configString =
    """
      | akka {
      |   loglevel = "DEBUG"
      | }
      |""".stripMargin
  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))



}
