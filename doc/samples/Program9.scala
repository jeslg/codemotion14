package programs


/* Kleisli combinators and conversions */

trait KleisliConversions extends LoggingCombinators2{
  import scala.language.implicitConversions

  implicit def transformLog[A,B](f: A => Logging[B]): A => Logging[Option[B]] = 
    (a: A) => f(a) concat { (b: B) => Return(Option(b)) }

  implicit def transformOpt[A,B](f: A => Option[B]): A => Logging[Option[B]] = 
    (a: A) => Return(f(a))

  implicit def transform[A,B](f: A => B): A => Logging[Option[B]] = 
    transformOpt(f andThen {(b: B) => Option(b)})

}

trait KleisliCombinators extends LoggingCombinators2{
  
  def if_K[A,B,C](
    cond:   A => Logging[Option[Boolean]])(
    then_K: B => Logging[Option[C]],
    else_K: B => Logging[Option[C]]): A => B => Logging[Option[C]] = 
    (a: A) => (b: B) => 
      cond(a) concatOption { (bool: Boolean) => 
        if (bool) then_K(b) else else_K(b)
      }

  def if2_K[A,B](
    cond:   A => Logging[Option[Boolean]])(
    then_K: A => Logging[Option[B]],
    else_K: A => Logging[Option[B]]): A => Logging[Option[B]] = 
    (a: A) => (if_K(cond)(then_K, else_K))(a)(a)

}

trait EffectsFunctionsWithKleisliCombinators extends EffectsFunctionsWithComposition 
  with KleisliEffect
  with KleisliCombinators
  with KleisliConversions{

  def authorized: String => Boolean = 
    (s: String) => s.length < 5

  def main2: String => Logging[Option[Int]] = 
    if2_K(authorized)(
      then_K = composeK(factorial, parseInt),
      else_K = { (s: String) => Error(s"Possible number too large: $s", Return(None)) })

}

object Program9 extends EffectsFunctionsWithKleisliCombinators with EffectInterpreter






