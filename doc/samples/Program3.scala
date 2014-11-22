package programs

/* Logging effect. */

trait LoggingInstructions{

  sealed trait Logging[A]
  case class Debug[A](msg: String, next: Logging[A]) extends Logging[A]
  case class Error[A](msg: String, next: Logging[A]) extends Logging[A]
  case class Return[A](value: A) extends Logging[A]

}

trait LoggingCombinators1 extends LoggingInstructions{

  def pure[A](a: A): Logging[A] = 
    Return(a)

  implicit class ExtendedLogging1[A](logging: Logging[A]){

    def concat[B](f: A => Logging[B]): Logging[B] = logging match{
      case Debug(msg, next) => Debug(msg, next concat f)
      case Error(msg, next) => Error(msg, next concat f)
      case Return(a) => f(a)
    }

  }

}


trait LoggerFunctions extends LoggingCombinators1{

  def parseInt(s: String): Logging[Int] = 
    Debug(s"Parsing $s", 
      Return(Integer.parseInt(s)))

  def factorial(n: Int): Logging[Int] = 
    if (n == 0) 
      Debug(s"factorial($n)=1", Return(1))
    else {
      factorial(n-1) concat {
        (rec_result: Int) => 
          val result = n * rec_result
          Debug(s"factorial($n)=$result", Return(result))
      }
    }

  def main: String => Logging[Int] = 
    (s: String) => {
      parseInt(s) concat {
        (number: Int) => factorial(number)
      }
    }

}

trait LoggerInterpreter{ self: LoggingInstructions => 

  def loggerInterpreter[T](logging: => Logging[T]): Unit = 
    logging match {
      case Debug(msg, next) => 
        println(s"Log: $msg")
        loggerInterpreter(next)
      case Error(msg, next) => 
        println(s"Log: $msg")
        loggerInterpreter(next)
      case _ => ()
    }

}

object Program3 extends LoggerFunctions with LoggerInterpreter
