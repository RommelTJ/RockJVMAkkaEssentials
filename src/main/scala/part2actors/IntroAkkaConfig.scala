package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
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
      |   loglevel = "ERROR"
      | }
      |""".stripMargin
  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])
  actor ! "A message to remember"

  // Method 2 - File configuration (most common)
  // By default, Akka looks at src > main > resources > application.conf
  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileSystem")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor ! "Remember me"

}
