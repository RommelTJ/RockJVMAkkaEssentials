package part1recap

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

}
