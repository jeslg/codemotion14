package programs


/* Option effects */

trait OptionFunctions extends LoggingInstructions{

  def parseInt(s: String): Option[Int] = 
    try{
      Some(Integer.parseInt(s))
    } catch {
      case _: NumberFormatException => 
        None
    }

  def factorial(n: Int): Option[Int] = 
    if (n < 0) 
      None
    else if (n == 0) 
      Some(1)
    else {
      factorial(n-1) flatMap {
        (rec_result: Int) => Some(n * rec_result)
      }
    }

  def main: String => Option[Int] = 
    (s: String) => {
      parseInt(s) flatMap {
        (number: Int) => factorial(number)
      }
    }

}

trait OptionInterpreter{ 

  def optionInterpreter[T](result: => Option[T]): Unit = 
    result match {
      case None => println("Computation: FAILED")
      case Some(result) => println(s"Computation: $result")
    }

}

object Program4 extends OptionFunctions with OptionInterpreter
