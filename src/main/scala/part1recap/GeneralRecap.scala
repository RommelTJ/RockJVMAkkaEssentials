package part1recap

import scala.annotation.tailrec

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


}
