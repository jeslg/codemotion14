package programs


/* Kleisli composition for logging */

trait KleisliLogging extends LoggerHelpers1{
  
  type ~>[A,B] = A => Logging[B]

  def composeKLog[A,B,C](g: B ~> C, f: A ~> B): A ~> C = 
    (a: A) => f(a) concat { (b: B) => g(b) }

}

trait LoggerFunctionsWithKleisli extends LoggerFunctions with KleisliLogging{

  def main2: String => Logging[Int] = 
    composeKLog(factorial, parseInt)

}

object Program7 extends LoggerFunctionsWithKleisli with EffectInterpreter






