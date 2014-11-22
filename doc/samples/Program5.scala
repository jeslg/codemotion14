package programs


/* Logging plus option effects. */

trait EffectsFunctions extends LoggingCombinators1{

  def parseInt(s: String): Logging[Option[Int]] = 
    try{
      Debug(s"Parsing $s", Return(Option(Integer.parseInt(s))))
    } catch {
      case _: NumberFormatException => 
        Error(s"'$s' is not an integer", Return(None))
    }

  def factorial(n: Int): Logging[Option[Int]] = 
    if (n < 0) 
      Error(s"factorial($n) = error: negative number", Return(None))
    else if (n == 0) 
      Debug(s"factorial($n)=1", Return(Some(1)))
    else {
      factorial(n-1) concat {
        case None => 
          Return(None)
        case Some(rec_result) => 
          val result = n * rec_result
          Debug(s"factorial($n)=$result", Return(Option(result)))
      }
    }

  def main: String => Logging[Option[Int]] = 
    (s: String) => {
      parseInt(s) concat {
        case None => Return(None)
        case Some(number: Int) => factorial(number)
      }
    }

}

trait EffectInterpreter extends LoggerInterpreter with OptionInterpreter{ self: LoggingCombinators1 => 

  def returned[A](logging: Logging[A]): A = 
    logging match {
      case Debug(_, next) => returned(next)
      case Error(_, next) => returned(next)
      case Return(a) => a
    }

  def interpreter[T](result: => Logging[Option[T]]): Unit = {
    loggerInterpreter(result)
    optionInterpreter(returned(result))
  }

}

object Program5 extends EffectsFunctions with EffectInterpreter
