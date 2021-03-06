package part1recap

import scala.concurrent.Future

object AdvancedRecap extends App {
  // Partial Functions
  // A partial function that only works for values 1, 2, and 5. Throws exception for other values.
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  // equivalent as the above
  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  // Partial Functions are extensions from regular functions (in this case of type Int => Int)
  val function: (Int => Int) = partialFunction

  // Collections may operate on partial functions
  val modifiedList = List(1, 2, 3).map({
    case 1 => 42
    case _ => 0
  })
  // Same but with common syntactic sugar
  val modifiedListCool = List(1, 2, 3).map {
    case 1 => 42
    case _ => 0
  }

  // Lifting
  val lifted = partialFunction.lift // total function from Int => Option[Int]
  lifted(2) // Some(65)
  lifted(999) // None

  // orElse
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }
  pfChain(5) // 999 per partialFunction
  pfChain(60) // 9000 per pfChain
  pfChain(457) // throws MatchError

  // Type Aliases
  type ReceiveFunction = PartialFunction[Any, Unit]

  // You can use "ReceiveFunction" or "PartialFunction[Any, Unit]" interchangeably
  def receive: ReceiveFunction = {
    case 1 => println("Hello")
    case _ => println("Confused")
  }
  def receive2: PartialFunction[Any, Unit] = {
    case 1 => println("Hello")
    case _ => println("Confused")
  }

  // implicits
  implicit val timeout: Int = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int): Unit = f()
  setTimeout(() => println("timeout"))(timeout)
  // can omit extra parameter list because the implicit value was defined before and the compiler uses it and
  // passes it into our function for us
  setTimeout(() => println("timeout"))

  // Implicit Conversions
  // 1) implicit defs
  case class Person(name: String) {
    def greet: String = s"Hi, my name is $name"
  }
  implicit def fromStringToPerson(string: String): Person = Person(string)
  // Compiler runs implict def on the String, then calls greet method.
  "Peter".greet // fromStringToPerson("Peter").greet()

  // 2) implicit classes
  implicit class Dog(name: String) {
    def bark: Unit = println("Bark")
  }
  // Compiler automatically converts string into Dog, then calls the bark method.
  "Lassie".bark // new Dog("Lassie").bark()

  // Organize - Implicit conversions can get confusing if they're not organized.
  // You can define how the compiler behaves.
  // The compiler chooses in this order: local scope > imported scope > companion objects.
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1, 2, 3).sorted // List(3, 2, 1) -> Because "inverseOrdering" is in "local scope".
  // Local Scope = The scope where the method with the implicit parameter is being called.

  // Imported scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("Hello, Future")
  }

  // Companion Objects of the types included in the call
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }
  List(Person("Bob"), Person("Alice")).sorted // List(Person("Alice"), Person("Bob"))

}
