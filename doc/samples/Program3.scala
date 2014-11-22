package programs


/* Side effects */

trait Program3{

  def factorial(n: Int): Int = 
    if (n < 0) 
      throw new IllegalArgumentException
    else {
      val result = if (n==0) 1 else n * factorial(n-1)
      println(s"factorial($n)=$result")
      result
    }  

}

/* Logging effect. */

trait DataTypes{

  sealed trait Logging[A]
  case class Debug[A](msg: String, next: Logging[A]) extends Logging[A]
  case class Error[A](msg: String, next: Logging[A]) extends Logging[A]
  case class Return[A](value: A) extends Logging[A]

}

trait LoggerHelpers1 extends DataTypes{

  implicit class ExtendedLogging1[A](logging: Logging[A]){

    def returned: A = logging match {
      case Debug(_, next) => next.returned
      case Error(_, next) => next.returned
      case Return(a) => a
    }

    def changeValue[B](f: A => B): Logging[B] = logging match{
      case Debug(msg, next) => Debug(msg, next changeValue f)
      case Error(msg, next) => Error(msg, next changeValue f)
      case Return(a) => Return(f(a))
    }

    def concat[B](f: A => Logging[B]): Logging[B] = logging match{
      case Debug(msg, next) => Debug(msg, next concat f)
      case Error(msg, next) => Error(msg, next concat f)
      case Return(a) => f(a)
    }

  }

}


trait LoggerFunctions extends DataTypes with LoggerHelpers1{

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

trait LoggerInterpreter{ self: DataTypes => 

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

object LoggerProgram extends LoggerFunctions with LoggerInterpreter
