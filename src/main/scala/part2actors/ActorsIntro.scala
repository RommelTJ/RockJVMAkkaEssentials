package part2actors

import akka.actor.ActorSystem

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

}
