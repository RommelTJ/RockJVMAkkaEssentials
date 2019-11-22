package part1recap

object MultithreadingRecap extends App {

  // Creating threads on the JVM

  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })
  val aThread2 = new Thread(() => println("I'm running in parallel"))
  aThread2.start()
  aThread2.join()

  // threads are unpredictable
  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))
  threadHello.start()
  threadGoodbye.start()
  // Different runs produce different results!

  // volatile locks amount for read-write, but only works for primitive types
  class BankAccount(@volatile private var amount: Int) {
    override def toString: String = "" + amount
    def withdraw(money: Int): Unit = this.amount -= money

    def safeWithdraw(money: Int): Unit = this.synchronized { // thread-safe
      this.amount -= money
    }
  }
  /*
  BA (10000)
  T1 -> withdraw 1000
  T2 -> withdraw 2000

  T1 -> this.amount = this.amount - .... // preempted by the OS
  T2 -> this.amount = this.amount - 2000 = 8000
  T1 -> -1000 = 9000
  => result = 9000

  this.amount = this.amount - 1000 is NOT ATOMIC
   */

  // Inter-thread communication on the JVM
  // wait - notify mechanism

}
