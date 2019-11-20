package part1recap

import scala.annotation.tailrec
import scala.util.Try

object GeneralRecap extends App {

  val aCondition: Boolean = false
  var aVariable = 42
  aVariable += 1 // aVariable = 43

  // expressions
  val aConditionedVal = if (aCondition) 42 else 65 // 65

  // code block
  val aCodeBlock = {
    if (aCondition) 74
    56
  } // 56

  // types
  // Unit
  val theUnit = println("Hello, Scala")

  // functions
  def aFunction(x: Int): Int = x + 1

  // recursion - TAIL recursion
  @tailrec
  def factorial(n: Int, acc: Int): Int = {
    if (n <= 0) acc
    else factorial(n - 1, acc * n)
  }

  // OOP
  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  // Abstract Classes and Traits
  trait Carnivore {
    def eat(a: Animal): Unit // abstract
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("Chomp! chomp!")
  }

  // Method notations
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog

  // Anonymous Classes
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("ROAR")
  }
  aCarnivore eat aDog

  // Generics
  abstract class MyList[+A]

  // Companion objects
  object MyList

  // Case Classes
  case class Person(name: String, age: Int)

  // Exceptions
  val aPotentialFailure = try {
    throw new RuntimeException("I'm innocent, I swear!") // of type "Nothing", not "Unit" or "Exception"
  } catch {
    case e: Exception => "I caught an exception!"
  } finally {
    // side-effects
    println("Some logs")
  }

  // Functional Programming
  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }
  val incremented = incrementer(42) // 43
  // Scala compiler apply trick: incrementer.apply(42)

  val anonymousIncrementer = (x: Int) => x + 1
  // Int => Int === Function1[Int, Int]

  // FP is all about working with functions as first-class
  List(1, 2, 3).map(incrementer)
  // map = HOF (Higher Order Function)

  // for-comprehensions
  val pairs = for {
    num <- List(1, 2, 3, 4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char
  // List(1, 2, 3, 4).flatMap(num => List('a', 'b', 'c', 'd').map(char => num + "-" + char))

  // Seq, Array, List, Vector, Map, Tuples, Sets

  // "collections"
  // Options and Try
  val anOption = Some(2)
  val aTry = Try {
    throw new RuntimeException
  }

  // Pattern Matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }
  val bob = Person("Bob", 42)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => "I don't know my name."
  }

}
