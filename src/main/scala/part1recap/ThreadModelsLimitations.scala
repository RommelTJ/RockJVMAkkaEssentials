package part1recap

import scala.concurrent.Future

object ThreadModelsLimitations extends App {

  // Rants

  // 1 - OOP encapsulation is only valid in the single threaded model.
  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount
    def withdraw(money: Int): Unit = this.amount -= money
    def deposit(money: Int): Unit = this.amount += money
    def getAmount(): String = s"$amount"
  }

//  val account = new BankAccount(2000)
//  for (_ <- 1 to 1000) {
//    new Thread(() => account.withdraw(1)).start()
//  }
//  for (_ <- 1 to 1000) {
//    new Thread(() => account.deposit(1)).start()
//  }
//  println(account.getAmount()) // result: 1,998.
  // OOP encapsulation is broken in a multithreading environment.
  // To fix, involves encapsulation. Locks to the rescue.

  class BankAccount2(private var amount: Int) {
    override def toString: String = "" + amount
    def withdraw(money: Int): Unit = this.synchronized {
      this.amount -= money
    }
    def deposit(money: Int): Unit = this.synchronized {
      this.amount += money
    }
    def getAmount(): String = s"$amount"
  }

//  val account2 = new BankAccount2(2000)
//  for (_ <- 1 to 1000) {
//    new Thread(() => account2.withdraw(1)).start()
//  }
//  for (_ <- 1 to 1000) {
//    new Thread(() => account2.deposit(1)).start()
//  }
//  println(account2.getAmount()) // result: 1,999.

  // But this also introduces deadlocks, livelocks and complex code.

  // 2 - Delegating something to a thread is a pain.

  // You have a running thread and you want to pass a Runnable to that thread. How?
  var task: Runnable = null;
  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task...")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[background] I have a task!")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable): Unit = {
    if (task == null) task = r

    runningThread.synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(500)
  delegateToBackgroundThread(() => println("42"))
  Thread.sleep(1000)
  delegateToBackgroundThread(() => println("This should run in the background"))


  // 3 - Tracing and dealing with errors in a multithreading environment is a pain

  // 1 million numbers in between 10 threads
  import scala.concurrent.ExecutionContext.Implicits.global
  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1)) // 0 - 99999, 100000 - 199999, 200000 - 299999, etc
    .map(range => Future {
      if (range.contains(546732)) throw new RuntimeException("Invalid number")
      range.sum
    })
  val sumFuture = Future.reduceLeft(futures)(_ + _) // Future with the sum of all the numbers
  sumFuture.onComplete(println)

}
