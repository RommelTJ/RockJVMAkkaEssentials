package part2actors

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

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
