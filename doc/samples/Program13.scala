package programs.scalaz

import scalaz.{Functor, Monad, Free, Coyoneda}
import Free.FreeC
import Coyoneda.CoyonedaF

/* Free monads */

trait LoggingInstructionsC{

  sealed trait LoggingFunctor[T]
  case class Debug[T](msg: String, next: T) extends LoggingFunctor[T]
  case class Error[T](msg: String, next: T) extends LoggingFunctor[T]

  type Logging[T] = FreeC[LoggingFunctor, T]

  def debug(msg: String): Logging[Unit] = Free.liftFC(Debug(msg,()))
  def error(msg: String): Logging[Unit] = Free.liftFC(Error(msg,()))
  def returns[T](value: T): Logging[T] = Free.point[CoyonedaF[LoggingFunctor]#A, T](value)

  implicit class LoggingOperators[U](logging: Logging[U]){
    def returns[T](value: T): Logging[T] = 
      logging map (_ => value)
  }

}

trait EffectsFunctionsC{ this: LoggingInstructionsC => 

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

trait EffectInterpreterC extends programs.OptionInterpreter{ 
  self: LoggingInstructionsC => 

  def loggerInterpreter[T](logging: => Logging[T]): T = 
    logging.go{ cy => cy.fi match {
      case Debug(msg, next) => 
        println(s"Log: $msg")
        cy.k(next)
      case Error(msg, next) => 
        println(s"Log: $msg")
        cy.k(next)
    }}

  def interpreter[T](result: => Logging[Option[T]]): Unit = 
    optionInterpreter(loggerInterpreter(result))

}

object Program13 extends LoggingInstructionsC 
  with EffectsFunctionsC 
  with EffectInterpreterC
  {
    import Coyoneda.CoyonedaF
    // implicit val m = Free.freeMonad[({type λ[α] = Coyoneda[LoggingFunctor, α]})#λ]
    // implicit val m = Free.freeMonad[CoyonedaF[LoggingFunctor]#A]
    // import Free.freeMonad
    // implicitly[Monad[({type f[x]=FreeC[LoggingFunctor,x]})#f]](Free.freeMonad[CoyonedaF[LoggingFunctor]#A])
    import Free.freeMonad
    // freeMonad[CoyonedaF[LoggingFunctor]#A]
    // implicitly[Functor[CoyonedaF[LoggingFunctor]#A]]
    // implicitly[Monad[({type f[x]=Free[CoyonedaF[LoggingFunctor]#A,x]})#f]]
    // implicitly[Monad[({type f[x]=Free[CoyonedaF[LoggingFunctor]#A,x]})#f]](Free.freeMonad[CoyonedaF[LoggingFunctor]#A])
    // implicitly[Monad[({type f[x]=FreeC[LoggingFunctor,x]})#f]]
  }


