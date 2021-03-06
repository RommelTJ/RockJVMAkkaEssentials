package part2actors

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}

object ChildActorsExercise extends App {

  // Distributed Word Counting
  val system = ActorSystem("ChildActorsExercise")

  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        println(s"[MASTER] Creating $nChildren children.")
        val childrenRefs = for (n <- 0 until nChildren) yield context.actorOf(Props[WordCounterWorker], s"wcw$n")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(
      childrenRefs: Seq[ActorRef],
      currentChildIndex: Int,
      currentTaskId: Int,
      requestMap: Map[Int, ActorRef]
    ): Receive = {
      case text: String =>
        println(s"[MASTER] I have received: $text. I will send it to child with ID: $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        // problem. Who should I send this to? Sender()? no. It should be the original requester of work.
        println(s"[MASTER] I have received a reply for task with ID #$id and the count is $count.")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"[CHILD] ${self.path}: I have received a task with ID: $id and Text: $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  /*
  Flow of exercise:
  Create a WordCounterMaster
  Send Initialize(10) to WordCounterMaster -> Create 10 workers
  Send "Akka is awesome" to wordCounterMaster
  -> WCM will send a WordCountTask("...") to one of its children
   -> Child replies with a WordCountReply(3) to the master
  -> Master replies with 3 to the sender

  requester -> WCM -> WCW
          r <- WCM <-
   */

  // Round-robin logic
  // 1, 2, 3, 4, 5 and 7 tasks -> 1, 2, 3, 4, 5, 1, 2

  // Hint: You might need to pass some extra information to WordCountTask and WordCountReply.

  class TestActor extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love Akka.", "Scala is super dope", "yes", "me too")
        texts.foreach(text => master ! text)
      case count: Int => println(s"[TEST ACTOR] I received a reply with count: $count")
    }
  }

  // Testing
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"

}
