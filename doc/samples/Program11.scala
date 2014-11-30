package programs.scalaz

import scala.language.implicitConversions

import scalaz._
import Scalaz._

/* OptionT monad transformer */

trait EffectsFunctionsWithComposition{ this: LoggingInstructions => 

  implicit def toOptionTLogging[T](logging: Logging[Option[T]]): OptionT[Logging,T] = 
    OptionT(logging)

  def parseInt(s: String): OptionT[Logging,Int] = 
    try{
      debug(s"Parsing $s") returns Option(Integer.parseInt(s))
    } catch {
      case _: NumberFormatException => 
        error(s"'$s' is not an integer") returns Option.empty[Int]
    }

  def factorial(n: Int): OptionT[Logging,Int] = 
    if (n < 0) 
      error(s"factorial($n) = error: negative number") returns Option.empty[Int]
    else if (n == 0) 
      debug(s"factorial($n)=1") returns Option(1)
    else {
      factorial(n-1) flatMap {
        case rec_result => 
          val result = n * rec_result
          debug(s"factorial($n)=$result") returns Option(result)
      }
    }

  def main: String => OptionT[Logging,Int] = 
    (s: String) => 
      parseInt(s) flatMap {
        case number: Int => factorial(number)
      } 

}

object Program11 extends LoggingInstructions 
  with EffectsFunctionsWithComposition 
  with EffectInterpreter


