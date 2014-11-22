package programs

/* Combining effects. */

trait LoggingCombinators2 extends LoggingCombinators1{

  implicit class ExtendedLogging2[A](logging: Logging[A]){

    def concatOption[B,C](f: B => Logging[Option[C]])(implicit ev: A <:< Option[B]): Logging[Option[C]] = 
      logging concat {
        case _: None.type => Return(None)
        case Some(b: B@unchecked) => f(b)
      }

  }

}

trait EffectsFunctionsWithComposition extends LoggingCombinators2{

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
      factorial(n-1) concatOption {
        (rec_result: Int) => 
          val result = n * rec_result
          Debug(s"factorial($n)=$result", Return(Option(result)))
      }
    }

  def main: String => Logging[Option[Int]] = 
    (s: String) => 
      parseInt(s) concatOption {
        (number: Int) => factorial(number)
      }
}


object Program6 extends EffectsFunctionsWithComposition with EffectInterpreter
