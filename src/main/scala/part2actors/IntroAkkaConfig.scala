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

  // Method 3 - Separate Configuration in same File
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigSystem", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "I'm special"

  // Method 4 - Separate Configuration in another file
  val separateConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"Separate Config Log Level: ${separateConfig.getString("akka.loglevel")}")

  // Method 5 - Different File Formats (JSON, Properties)
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"Separate JSON Config property: ${jsonConfig.getString("aJsonProperty")}")
  println(s"Separate JSON Config property: ${jsonConfig.getString("akka.loglevel")}")

}
