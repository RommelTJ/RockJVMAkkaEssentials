package part2actors

import akka.actor.ActorSystem

object ActorsIntro extends App {

  // Part 1 - Actor Systems

  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

}
