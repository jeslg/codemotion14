object Samples0{

  def fib(n: Int): Int = 
    if (n==0) 0
    else if (n==1) 1
    else fib(n-1)+fib(n-2)

  def parseInt(s: String): Int = 
    Integer.parseInt(s)

}


trait Samples1Functions{ self =>
  import scala.annotation.tailrec

  def parseInt(s: String): Option[Int] = 
    try{
      Some(Integer.parseInt(s))
    } catch {
      case _ : NumberFormatException => None
    }

  def fib(n: Int): Option[Int] = {
    
    @tailrec
    def fib_aux(fib_0: Int, fib_1: Int, i: Int): Int = 
      if (i==n) fib_0
      else if (i+1==n) fib_1
      else fib_aux(fib_1, fib_0+fib_1, i+1)

    if (n < 0) None
    else Some(fib_aux(0,1,0))
  }

  def composeK[A,B,C](g: B => Option[C], f: A => Option[B]): A => Option[C] = 
    (a: A) => f(a) match {
      case None => None
      case Some(b) => g(b)
    }

  implicit class OptionHelpers[B,C](g: B => Option[C]){
    def composeK[A](f: A => Option[B]): A => Option[C] = 
      self.composeK(g,f)
  }

}

object Samples1 extends Samples1Functions



trait Samples2Functions extends Samples1Functions{
  import scala.util.{Try, Success, Failure}

  def parseIntT(s: String): Try[Int] = 
    Try(Integer.parseInt(s))

  def transformN[A,B](f: A => Try[B]): A => Option[B] = 
    (a: A) => f(a) match {
      case Failure(_) => None
      case Success(b) => Some(b)
    }

}

object Samples2 extends Samples2Functions


trait Samples3Functions extends Samples2Functions{
  import scala.util.Try

  override def composeK[A,B,C](g: B => Option[C], f: A => Option[B]): A => Option[C] = 
    (a: A) => f(a) flatMap g
  
  override def transformN[A,B](f: A => Try[B]): A => Option[B] = 
    (a: A) => f(a).toOption

}

object Samples3 extends Samples3Functions

object Samples4{
  import scala.util.Try
  
  val k1 = Kleisli((x: String) => Try(Integer.parseInt(x)).toOption)

  val k2 = Kleisli((i: Int) => Option(i+1))

  // k2 <=< k1
}



