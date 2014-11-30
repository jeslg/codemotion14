package programs.scalaz

import scalaz._
import Scalaz._

/* Free monads */

trait LoggingInstructions{

  sealed trait LoggingFunctor[T]
  case class Debug[T](msg: String, next: T) extends LoggingFunctor[T]
  case class Error[T](msg: String, next: T) extends LoggingFunctor[T]

  implicit val functorLogf = new Functor[LoggingFunctor]{
    def map[A, B](fa: LoggingFunctor[A])(f: A => B): LoggingFunctor[B] = 
      fa match {
        case Debug(msg1, next) => Debug(msg1, f(next))
        case Error(msg1, next)  => Error(msg1, f(next))
      }
  }

  type Logging[T] = Free[LoggingFunctor, T]

  def debug(msg: String): Logging[Unit] = Free.liftF(Debug(msg,()))
  def error(msg: String): Logging[Unit] = Free.liftF(Error(msg,()))
  def returns[T](value: T): Logging[T] = Free.point(value)

  implicit class LoggingOperators[U](logging: Logging[U]){
    def returns[T](value: T): Logging[T] = 
      logging map (_ => value)
  }

}

trait EffectsFunctions{ this: LoggingInstructions => 

  def parseInt(s: String): Logging[Option[Int]] = 
    try{
      debug(s"Parsing $s") returns Option(Integer.parseInt(s))
    } catch {
      case _: NumberFormatException => 
        error(s"'$s' is not an integer") returns None
    }

  def factorial(n: Int): Logging[Option[Int]] = 
    if (n < 0) 
      error(s"factorial($n) = error: negative number") returns None
    else if (n == 0) 
      debug(s"factorial($n)=1") returns Some(1)
    else {
      factorial(n-1) flatMap {
        case None => 
          returns(None)
        case Some(rec_result) => 
          val result = n * rec_result
          debug(s"factorial($n)=$result") returns Option(result)
      }
    }

  def main: String => Logging[Option[Int]] = 
    (s: String) => 
      parseInt(s) flatMap {
        case None => 
          returns(None)
        case Some(number: Int) => 
          factorial(number)
      }

}

trait EffectInterpreter extends programs.OptionInterpreter{ 
  self: LoggingInstructions => 

  def loggerInterpreter[T](logging: => Logging[T]): T = 
    logging.go{
      case Debug(msg, next) => 
        println(s"Log: $msg")
        next
      case Error(msg, next) => 
        println(s"Log: $msg")
        next
    }

  def interpreter[T](result: => Logging[Option[T]]): Unit = 
    optionInterpreter(loggerInterpreter(result))

}

object Program10 extends LoggingInstructions 
  with EffectsFunctions 
  with EffectInterpreter


