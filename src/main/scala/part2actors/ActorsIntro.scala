package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {

  // Part 1 - Actor Systems

  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // Part 2 - Create Actors

  // Rules of Actors:
  // Actors are uniquely identified
  // Messages are asynchronous
  // Each actor may respond differently
  // Actors are encapsulated

  // word count actor
  class WordCountActor extends Actor {
    // internal data
    var totalWords = 0

    // behavior
    override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word counter] I have received $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // Part 3 - Instantiate an Actor

  val wordCounter: ActorRef = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // Part 4 - Communicate

  wordCounter ! "I am learning Akka and it's pretty damn cool!"
  wordCounter.!("I am learning Akka and it's pretty damn cool!") // equivalent

  // Sending this message is asynchronous
  anotherWordCounter ! "A different message"

}
