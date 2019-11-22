package part1recap

object MultithreadingRecap extends App {

  // Creating threads on the JVM

  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })
  val aThread2 = new Thread(() => println("I'm running in parallel"))
  aThread2.start()
  aThread2.join()

}
