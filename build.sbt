name := "RockJVMAkkaEssentials"

version := "0.1"

scalaVersion := "2.13.0"

val akkaVersion = "2.6.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)
