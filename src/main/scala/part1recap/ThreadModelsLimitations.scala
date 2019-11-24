package part1recap

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

}
