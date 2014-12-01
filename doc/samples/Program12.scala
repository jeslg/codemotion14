package programs.scalaz

import scala.language.implicitConversions

import scalaz._
import Scalaz._

/* Kleisli composition */

trait EffectsFunctionsWithKleisli { this: LoggingInstructions => 

  implicit def toOptionTLogging[T](logging: Logging[Option[T]]): OptionT[Logging,T] = 
    OptionT(logging)

  type EffectT[T] = OptionT[Logging, T]
  type ~>[A,B] = Kleisli[EffectT, A, B]

  def parseInt: String ~> Int = Kleisli((s: String) => 
    try{
      debug(s"Parsing $s") returns Option(Integer.parseInt(s))
    } catch {
      case _: NumberFormatException => 
        error(s"'$s' is not an integer") returns Option.empty[Int]
    }
  )

  def factorial: Int ~> Int = Kleisli( (n: Int) => 
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
  )

  def myMain: String ~> Int = 
    factorial <=< parseInt

}

object Program12 extends App 
  with LoggingInstructions 
  with EffectsFunctionsWithKleisli 
  with EffectInterpreter{

  interpreter(myMain("3").run)

}






