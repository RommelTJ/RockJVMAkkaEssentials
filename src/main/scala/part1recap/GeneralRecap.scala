package part1recap

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

}
