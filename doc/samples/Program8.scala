package programs


/* Kleisli composition for full effects */

trait KleisliEffect extends ExtendedLogging2{
  
  def composeK[A,B,C](g: B => Logging[Option[C]], f: A => Logging[Option[B]]): A => Logging[Option[C]] = 
    (a: A) => f(a) concatOption { (b: B) => g(b) }

}

trait EffectsFunctionsWithKleisli extends EffectsFunctionsWithComposition with KleisliEffect{

  def main2: String => Logging[Option[Int]] = 
    composeK(factorial, parseInt)

}

object Program8 extends EffectsFunctionsWithKleisli with EffectInterpreter






